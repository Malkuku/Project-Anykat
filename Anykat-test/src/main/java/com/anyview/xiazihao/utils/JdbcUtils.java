package com.anyview.xiazihao.utils;

import com.anyview.xiazihao.config.DataSourceConfig;
import com.anyview.xiazihao.connectionPool.HakimiConnectionPool;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
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

    // 获取连接(从连接池)
    public static Connection getConnection() throws SQLException, FileNotFoundException {
        return HakimiConnectionPool.getInstance().getConnection();
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

    // 关闭资源
    private static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            log.error("Error closing resources: {}", e.getMessage());
        }
    }

    public static <T> T executeTransaction(Function<Connection, T> action) throws Exception {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 关闭自动提交，开始事务

            T result = action.apply(conn); // 执行事务中的操作
            conn.commit(); // 提交事务
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // 出现异常时回滚事务
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            throw e; // 重新抛出异常
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                    conn.close(); // 关闭连接
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // 事务回调接口
    public interface TransactionCallback<T> {
        T doInTransaction(Connection conn) throws SQLException;
    }
}

