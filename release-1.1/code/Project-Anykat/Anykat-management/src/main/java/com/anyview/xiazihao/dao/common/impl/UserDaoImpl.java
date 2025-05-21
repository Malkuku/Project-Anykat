package com.anyview.xiazihao.dao.common.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.common.UserDao;
import com.anyview.xiazihao.entity.param.pojo.UserQueryParam;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class UserDaoImpl implements UserDao {
    @Override
    public Integer selectUserCount(UserQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT COUNT(*)
        FROM user
        WHERE (#{username} IS NULL OR username LIKE CONCAT('%', #{username}, '%'))
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
            AND (#{role} IS NULL OR role = #{role})
        """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<User> selectUserByPage(UserQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM user
        WHERE (#{username} IS NULL OR username LIKE CONCAT('%', #{username}, '%'))
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
            AND (#{role} IS NULL OR role = #{role})
        ORDER BY updated_at DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                User.class,
                param
        );
    }

    @Override
    public User selectUserByUsername(String username) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM user
            WHERE username = ?
        """;
        List<User> users = JdbcUtils.executeQuery(
                sql,
                User.class,
                username
        );
        return users.isEmpty() ? null : users.get(0);
    }
}
