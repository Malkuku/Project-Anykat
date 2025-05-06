package com.anyview.xiazihao.utils;

import com.anyview.xiazihao.connectionPool.HakimiConnectionPool;
import com.anyview.xiazihao.connectionPool.ConnectionContext;
import com.anyview.xiazihao.sampleFlatMapper.KatSimpleMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class JdbcUtils {

    // 优先从事务上下文获取连接，没有则从连接池获取
    public static Connection getConnection() throws SQLException, FileNotFoundException {
        try {
            // 1. 尝试从事务上下文获取
            Connection txConn = ConnectionContext.getConnection();
            if (!txConn.isClosed()) {
                return txConn;
            }
            // 2. 没有事务则从连接池获取
            return HakimiConnectionPool.getInstance().getConnection();
        } catch (IllegalStateException e) {
            // 上下文未初始化时回退到连接池
            return HakimiConnectionPool.getInstance().getConnection();
        }
    }

    //打印sql和参数
    public static void printSqlAndParams(String sql, Object... params) {
        log.info("Executing SQL: {}", sql);
        log.info("Parameters: {}", (Object) params);
    }

    
    // 通用更新方法 (INSERT/UPDATE/DELETE)
    public static int executeUpdate(String sql, Object... params) throws SQLException, FileNotFoundException {
        printSqlAndParams(sql, params);
        Connection conn = getConnection();
        boolean isTxActive = ConnectionContext.isActive();
        printSqlAndParams(sql, params);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } finally {
            if (conn != null && !isTxActive) {
                conn.close();
            }
        }
    }

    // 通用查询方法 (手动结果映射)
    public static <T> List<T> executeQuery(String sql,Function<ResultSet, T> rowMapper,Object... params) throws SQLException, FileNotFoundException {
        printSqlAndParams(sql, params);
        Connection conn = getConnection();
        boolean isTxActive = ConnectionContext.isActive();
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
            if(conn != null && !isTxActive) {
                conn.close();
            }
        }
    }

    // 通用查询方法(自动化映射)
    public static <T> List<T> executeQuery(String sql, Class<T> clazz, Object... params) throws SQLException, FileNotFoundException {
        params = KatSimpleMapper.extractParamsFromEntity(sql,params[0]);
        sql = KatSimpleMapper.replaceParamPlaceholders(sql);
        printSqlAndParams(sql, params);
        Connection conn = getConnection();
        boolean isTxActive = ConnectionContext.isActive();

        ResultSet rs;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            rs = stmt.executeQuery();

            // 使用映射器直接映射结果集
            return KatSimpleMapper.map(rs, clazz);
        } finally {
            if (conn != null && !isTxActive) {
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

    //事务方法
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

