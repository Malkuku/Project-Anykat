package com.anyview.xiazihao.connectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接上下文（基于 ThreadLocal）
 */
public class ConnectionContext {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static Connection getConnection() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            throw new IllegalStateException("No active transaction connection found!");
        }
        return conn;
    }

    public static boolean isActive() {
        try {
            Connection conn = connectionHolder.get();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void bindConnection(Connection conn) {
        connectionHolder.set(conn);
    }

    public static void unbindConnection() {
        connectionHolder.remove();
    }
}
