package com.anyview.xiazihao.dao.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentCourseDao;
import com.anyview.xiazihao.entity.param.view.StudentCourseQueryParam;
import com.anyview.xiazihao.entity.view.StudentCourseView;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class StudentCourseDaoImpl implements StudentCourseDao {

    @Override
    public Integer selectStudentCourseCount(StudentCourseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT COUNT(*)
        FROM v_student_courses
        WHERE student_id = #{studentId}
            AND (#{courseName} IS NULL OR course_name LIKE CONCAT('%', #{courseName}, '%'))
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
    public List<StudentCourseView> selectStudentCoursesByPage(StudentCourseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM v_student_courses
        WHERE student_id = #{studentId}
            AND (#{courseName} IS NULL OR course_name LIKE CONCAT('%', #{courseName}, '%'))
            AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
        ORDER BY semester_start DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                StudentCourseView.class,
                param
        );
    }
}
