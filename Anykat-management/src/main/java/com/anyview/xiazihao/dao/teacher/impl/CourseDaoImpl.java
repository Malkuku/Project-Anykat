package com.anyview.xiazihao.dao.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.CourseDao;
import com.anyview.xiazihao.entity.param.CourseQueryParam;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class CourseDaoImpl implements CourseDao {
    @Override
    public Integer selectCourseCount(CourseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT COUNT(*)
        FROM course
        WHERE 1=1\s
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
            AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
        """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<Course> selectCourseByPage(CourseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM course
        WHERE 1=1\s
            AND (#{name} IS NULL OR name LIKE CONCAT('%', #{name}, '%'))
            AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
        ORDER BY updated_at DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                Course.class,
                param
        );
    }

    @Override
    public Course selectCourseById(Integer id) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM course
        WHERE id = ?""";
        List<Course> courses = JdbcUtils.executeQuery(sql, Course.class, id);
        return courses.isEmpty() ? null : courses.get(0);
    }
}
