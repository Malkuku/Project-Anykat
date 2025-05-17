package com.anyview.xiazihao.dao.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentCourseDao;
import com.anyview.xiazihao.entity.param.view.StudentCourseQueryParam;
import com.anyview.xiazihao.entity.view.StudentCourseProgress;
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

    @Override
    public StudentCourseProgress selectStudentCourseProgress(Integer studentId, Integer courseId, Integer exerciseStatus) throws SQLException, FileNotFoundException {
        String sql = """
                SELECT
                    SUM(total_questions) as total_questions,
                    SUM(completed_questions) as completed_questions,
                    SUM(total_score) as total_score,
                    SUM(completed_score) as completed_score
                FROM
                    v_student_course_progress
                WHERE
                    student_id = ?
                    AND (? IS NULL OR course_id = ?)
                    AND (? IS NULL OR exercise_status = ?)
                ORDER BY
                    exercise_id;
                """;
        return JdbcUtils.executeQuery(
                sql,
                StudentCourseProgress.class,
                studentId,
                courseId, courseId,
                exerciseStatus, exerciseStatus
        ).get(0);
    }
}
