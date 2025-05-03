package com.anyview.xiazihao.connectionPool;

import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.entity.config.HakimiConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HakimiConnectionPool {

    // 单例实例
    private static volatile HakimiConnectionPool instance;

    // 配置参数
    private final HakimiConfig hakimiConfig;

    // 连接存储（存储原始连接）
    private final BlockingQueue<Connection> idleConnections;
    private final Set<Connection> activeConnections;

    // 监控统计
    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AtomicInteger waitCount = new AtomicInteger(0);
    private volatile boolean initialized = false;

    private HakimiConnectionPool() throws FileNotFoundException {
        this.hakimiConfig = AppConfig.getInstance().getHakimi();
        this.idleConnections = new LinkedBlockingQueue<>(hakimiConfig.getMaxSize());
        this.activeConnections = ConcurrentHashMap.newKeySet();
    }

    public static HakimiConnectionPool getInstance() throws FileNotFoundException {
        if (instance == null) {
            synchronized (HakimiConnectionPool.class) {
                if (instance == null) {
                    instance = new HakimiConnectionPool();
                }
            }
        }
        return instance;
    }


    private final Object initLock = new Object();
    public void init() throws SQLException {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    // 初始化最小空闲连接
                    for (int i = 0; i < hakimiConfig.getMinIdle(); i++) {
                        idleConnections.add(createPhysicalConnection());
                    }
                    initialized = true;
                }
            }
        }
    }

    private void ensureInitialized() throws SQLException, FileNotFoundException {
        // 双重检查锁定模式确保线程安全
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    // 1. 检查单例实例是否已创建
                    if (instance == null) {
                        instance = getInstance();
                    }

                    // 2. 初始化连接池数据结构
                    this.init();

                    // 3. 创建初始连接
                    for (int i = 0; i < hakimiConfig.getMinIdle(); i++) {
                        try {
                            Connection conn = createPhysicalConnection();
                            idleConnections.offer(conn);
                        } catch (SQLException e) {
                            log.error("Failed to create initial connection", e);
                            // 如果创建初始连接失败，关闭已创建的连接
                            closeAllConnections();
                            throw e;
                        }
                    }

                    // 4. 标记为已初始化
                    initialized = true;
                    log.info("Connection pool initialized with {} idle connections", hakimiConfig.getMinIdle());
                }
            }
        }
    }

    /**
     * 关闭所有连接（用于初始化失败时清理）
     */
    private void closeAllConnections() {
        Connection conn;
        while ((conn = idleConnections.poll()) != null) {
            closeConnection(conn);
        }
        createdCount.set(0);
    }

    // 获取连接
    public Connection getConnection() throws SQLException, FileNotFoundException {
        ensureInitialized();

        Connection rawConn = null;
        try {
            // 1. 尝试获取空闲连接
            while ((rawConn = idleConnections.poll()) != null) {
                if (testConnection(rawConn)) {
                    break;
                }
                closeConnection(rawConn);
                createdCount.decrementAndGet();
                rawConn = null;
            }

            // 2. 创建新连接
            if (rawConn == null) {
                while (createdCount.get() < hakimiConfig.getMaxSize()) {
                    if (createdCount.compareAndSet(createdCount.get(), createdCount.get() + 1)) {
                        try {
                            rawConn = createPhysicalConnection();
                            break;
                        } catch (SQLException e) {
                            createdCount.decrementAndGet();
                            throw e;
                        }
                    }
                }
            }

            // 3. 等待可用连接
            if (rawConn == null) {
                waitCount.incrementAndGet();
                try {
                    rawConn = idleConnections.poll(hakimiConfig.getMaxWaitMillis(), TimeUnit.MILLISECONDS);
                    if (rawConn == null) {
                        throw new SQLException("Wait timeout");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted");
                } finally {
                    waitCount.decrementAndGet();
                }
            }

            // 验证连接有效性
            if (!testConnection(rawConn)) {
                closeConnection(rawConn);
                createdCount.decrementAndGet();
                return getConnection(); // 递归重试
            }

            activeConnections.add(rawConn);
            return createProxyConnection(rawConn);
        } catch (Exception e) {
            if (rawConn != null) {
                closeConnection(rawConn);
                createdCount.decrementAndGet();
            }
            throw e;
        }
    }

    private Connection createProxyConnection(Connection rawConn) {
        return (Connection) Proxy.newProxyInstance(
                PooledConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class, PooledConnection.class},
                new ConnectionInvocationHandler(rawConn)
        );
    }

    private class ConnectionInvocationHandler implements InvocationHandler {
        private final Connection rawConnection;
        private volatile boolean closed = false;
        private volatile long lastUsedTime = System.currentTimeMillis();

        public ConnectionInvocationHandler(Connection rawConn) {
            this.rawConnection = rawConn;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            lastUsedTime = System.currentTimeMillis();

            switch (method.getName()) {
                case "close" -> {
                    if (!closed) {
                        closed = true;
                        releaseConnection((Connection) proxy);
                    }
                    return null;
                }
                case "isClosed" -> {
                    return closed;
                }
                case "getRawConnection" ->
                        throw new UnsupportedOperationException("Direct access to raw connection is prohibited");
                case "getLastUsedTime" -> {
                    return lastUsedTime;
                }
                case "realClose" -> {
                    closeConnection(rawConnection);
                    createdCount.decrementAndGet();
                    return null;
                }
            }

            if (closed) {
                throw new SQLException("Connection already closed");
            }

            try {
                return method.invoke(rawConnection, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }


    private void releaseConnection(Connection proxyConn) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(proxyConn);
            if (handler instanceof ConnectionInvocationHandler invocationHandler) {
                Connection rawConn = invocationHandler.rawConnection;

                synchronized (activeConnections) {
                    if (!activeConnections.remove(rawConn)) {
                        return; // 连接已被其他线程释放
                    }
                }

                if (testConnection(rawConn)) {
                    resetConnection(rawConn);
                    if (!idleConnections.offer(rawConn)) {
                        closeConnection(rawConn);
                        createdCount.decrementAndGet();
                    }
                } else {
                    closeConnection(rawConn);
                    createdCount.decrementAndGet();
                }
            }
        } catch (Exception e) {
            log.error("Error releasing connection", e);
        }
    }

    // 创建物理连接
    private Connection createPhysicalConnection() throws SQLException {
        Connection rawConn = DriverManager.getConnection(
                hakimiConfig.getUrl(),
                hakimiConfig.getUsername(),
                hakimiConfig.getPassword()
        );
        createdCount.incrementAndGet();

        try {
            rawConn.setAutoCommit(true);
            rawConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return rawConn;
        } catch (SQLException e) {
            closeConnection(rawConn);
            throw e;
        }
    }

    // 测试连接有效性
    private boolean testConnection(Connection conn) {
        if (conn == null) return false;

        try {
            if (conn.isClosed()) {
                return false;
            }

            // 使用更高效的验证查询
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(1);
                return stmt.execute("/* ping */ SELECT 1");
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // 重置连接状态
    private void resetConnection(Connection conn) throws SQLException {
        if (!conn.getAutoCommit()) {
            conn.rollback();
            conn.setAutoCommit(true);
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // Connection 实现了 AutoCloseable
            } catch (SQLException e) {
                log.error("Error closing connection", e);
            }
        }
    }
}