package com.anyview.xiazihao.dao.common.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.common.ClassDao;
import com.anyview.xiazihao.entity.param.pojo.ClassQueryParam;
import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class ClassDaoImpl implements ClassDao {
    @Override
    public Integer selectClassCount(ClassQueryParam param) throws FileNotFoundException, SQLException {
        String sql = """
        SELECT COUNT(*)
        FROM class
        WHERE 1=1\s
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
        """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<Class> selectClassByPage(ClassQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM class
        WHERE 1=1\s
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
        ORDER BY updated_at DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                Class.class,
                param
        );
    }

    @Override
    public Class selectClassById(Integer id) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM class
        WHERE id = ?""";
        List<Class> classes = JdbcUtils.executeQuery(sql, Class.class, id);
        return classes.isEmpty() ? null : classes.get(0);
    }
}
