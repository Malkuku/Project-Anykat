package com.anyview.xiazihao.dao.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.SemesterDao;
import com.anyview.xiazihao.entity.param.SemesterQueryParam;
import com.anyview.xiazihao.entity.pojo.Semester;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class SemesterDaoImpl implements SemesterDao {
    @Override
    public Integer selectSemesterCount(SemesterQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT COUNT(*)
        FROM semester
        WHERE 1=1\s
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
            AND (#{startTime} IS NULL OR start_time >= #{startTime})
            AND (#{endTime} IS NULL OR end_time <= #{endTime})
        """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<Semester> selectSemesterByPage(SemesterQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM semester
        WHERE 1=1\s
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
            AND (#{startTime} IS NULL OR start_time >= #{startTime})
            AND (#{endTime} IS NULL OR end_time <= #{endTime})
        ORDER BY updated_at DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                Semester.class,
                param
        );
    }

    @Override
    public Semester selectSemesterById(Integer id) throws SQLException, FileNotFoundException, SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM semester
        WHERE id = ?""";
        List<Semester> semesters = JdbcUtils.executeQuery(sql, Semester.class, id);
        return semesters.isEmpty() ? null : semesters.get(0);
    }
}
