package com.anyview.xiazihao.utils;

import com.anyview.xiazihao.connectionPool.HakimiConnectionPool;
import com.anyview.xiazihao.connectionPool.ConnectionContext;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class JdbcUtils {

    // 优先从事务上下文获取连接，没有则从连接池获取
    public static Connection getConnection() throws SQLException, FileNotFoundException {
        try {
            // 1. 尝试从事务上下文获取
            Connection txConn = ConnectionContext.getConnection();
            if (txConn != null && !txConn.isClosed()) {
                return txConn;
            }
            // 2. 没有事务则从连接池获取
            return HakimiConnectionPool.getInstance().getConnection();
        } catch (IllegalStateException e) {
            // 上下文未初始化时回退到连接池
            return HakimiConnectionPool.getInstance().getConnection();
        }
    }

    // 通用查询方法（自动判断是否关闭连接）
    public static <T> List<T> executeQuery(String sql, Function<ResultSet, T> rowMapper, Object... params)
            throws SQLException, FileNotFoundException {
        Connection conn = getConnection();
        boolean isTxActive = ConnectionContext.isActive(); // 是否在事务中
        try {
            return executeQuery(conn, sql, !isTxActive, rowMapper, params);
        } finally {
            if (!isTxActive && conn != null) {
                conn.close(); // 非事务场景手动关闭
            }
        }
    }

    // 通用更新方法（自动判断事务）
    public static int executeUpdate(String sql, Object... params)
            throws SQLException, FileNotFoundException {
        Connection conn = getConnection();
        boolean isTxActive = ConnectionContext.isActive();
        try {
            return executeUpdate(conn, sql, !isTxActive, params);
        } finally {
            if (!isTxActive && conn != null) {
                conn.close();
            }
        }
    }

    // 通用查询方法 (动态SQL+参数)
    public static <T> List<T> executeQuery(Connection conn, String sql, boolean isAutoCloseConn ,Function<ResultSet, T> rowMapper,Object... params) throws SQLException {
        log.info("Executing SQL: {}", sql);
        log.info("Parameters: {}", (Object) params);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<T> result = new ArrayList<>();
        try {
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(rowMapper.apply(rs));
            }
            return result;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if(conn != null && isAutoCloseConn) {
               conn.close();
            }
        }
    }

    // 通用更新方法 (INSERT/UPDATE/DELETE)
    public static int executeUpdate(Connection conn, String sql,boolean isAutoCloseConn, Object... params) throws SQLException {
        log.info("Executing SQL: {}", sql);
        log.info("Parameters: {}", (Object) params);
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if(conn != null && isAutoCloseConn) {
                conn.close();
            }
        }
    }

    // 设置参数
    private static void setParameters(PreparedStatement stmt, Object... params)
            throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }

    // 改造事务方法（使用上下文管理）
    public static <T> T executeTransaction(Function<Connection, T> action) throws Exception {
        Connection conn = null;
        try {
            conn = getConnection();
            ConnectionContext.bindConnection(conn); // 绑定到上下文
            conn.setAutoCommit(false);

            T result = action.apply(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            ConnectionContext.unbindConnection();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}

