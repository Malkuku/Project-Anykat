package com.anyview.xiazihao.connectionPool;

import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.entity.config.HakimiConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
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

import static ch.qos.logback.core.util.CloseUtil.closeQuietly;

@Slf4j
public class HakimiConnectionPool {

    // 单例实例
    private static volatile HakimiConnectionPool instance;

    // 配置参数
    private final HakimiConfig hakimiConfig;

    // 连接存储
    private BlockingQueue<Connection> idleConnections;
    private Set<Connection> activeConnections;

    // 监控统计
    private AtomicInteger createdCount = new AtomicInteger(0);
    private AtomicInteger waitCount = new AtomicInteger(0);
    private volatile boolean initialized = false;

    // 私有构造函数
    private HakimiConnectionPool() throws FileNotFoundException {
        this.hakimiConfig = AppConfig.getInstance().getHakimi();
    }

    // 获取单例实例
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

    // 初始化连接池
    private synchronized void init() throws SQLException {
        if (initialized) {
            return;
        }
        idleConnections = new LinkedBlockingQueue<>(hakimiConfig.getMaxSize());
        activeConnections = ConcurrentHashMap.newKeySet();
        initialized = true;
    }

    // 获取连接
    public Connection getConnection() throws SQLException, FileNotFoundException {
        if (instance == null) {
            synchronized (HakimiConnectionPool.class) {
                if (instance == null) {
                    instance = new HakimiConnectionPool();
                }
            }
        }

        // 确保已初始化
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    init();
                }
            }
        }
        // 1. 尝试快速获取
        Connection conn = idleConnections.poll();
        if (conn != null) {
            activeConnections.add(conn);
            return wrapConnection(conn);
        }

        // 2. 尝试创建新连接
        if (createdCount.get() < hakimiConfig.getMaxSize()) {
            try {
                Connection newConn = createPhysicalConnection();
                activeConnections.add(newConn);
                return wrapConnection(newConn);
            } catch (SQLException e) {
                log.debug("Failed to create new connection: {}",e.getMessage());
                // 创建失败继续等待
            }
        }

        // 3. 进入等待
        waitCount.incrementAndGet();
        try {
            conn = idleConnections.poll(hakimiConfig.getMaxWaitMillis(), TimeUnit.MILLISECONDS);
            if (conn != null) {
                activeConnections.add(conn);
                return wrapConnection(conn);
            }
            throw new SQLException("Wait timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted");
        } finally {
            waitCount.decrementAndGet();
        }
    }

    // 释放连接
    public void releaseConnection(Connection conn) {
        if (!(conn instanceof PooledConnection)) {
            throw new IllegalArgumentException("Invalid connection");
        }

        PooledConnection pooled = (PooledConnection) conn;
        Connection rawConn = pooled.getRawConnection();

        activeConnections.remove(rawConn);

        try {
            if (testConnection(rawConn)) {
                resetConnection(rawConn);
                idleConnections.put(rawConn);
            } else {
                closeQuietly((Closeable) rawConn);
            }
        } catch (Exception e) {
            closeQuietly((Closeable) rawConn);
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
            closeQuietly((Closeable) rawConn);
            throw e;
        }
    }

    // 包装连接
    private Connection wrapConnection(Connection rawConn) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionInvocationHandler(rawConn)
        );
    }

    // 连接代理处理器
    private static class PooledConnection {
        private final Connection rawConnection;

        public PooledConnection(Connection rawConnection) {
            this.rawConnection = rawConnection;
        }

        public Connection getRawConnection() {
            return rawConnection;
        }
    }

    // 连接调用处理器
    private class ConnectionInvocationHandler implements InvocationHandler {
        private final Connection rawConnection;
        private volatile boolean closed = false;

        public ConnectionInvocationHandler(Connection rawConn) {
            this.rawConnection = rawConn;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                if (!closed) {
                    closed = true;
                    releaseConnection((Connection) proxy);
                }
                return null;
            }

            if ("isClosed".equals(method.getName())) {
                return closed;
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

    // 测试连接有效性
    private boolean testConnection(Connection conn) {
        try {
            if (conn.isClosed()) {
                return false;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(1);
                stmt.execute("SELECT 1");
                return true;
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
}