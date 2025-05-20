package com.anyview.xiazihao.dao.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherLinkDao;
import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.entity.pojo.Semester;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherLinkDaoImpl implements TeacherLinkDao {
    @Override
    public List<Class> getTeacherClasses(Integer teacherId, Integer semesterId) throws SQLException, FileNotFoundException {
        String sql = """
                SELECT DISTINCT
                     cl.id,
                     cl.name
                 FROM
                     class cl
                 JOIN
                     exercise_class ec ON cl.id = ec.class_id
                 JOIN
                     exercise e ON ec.exercise_id = e.id
                 JOIN
                     course co ON e.course_id = co.id
                 JOIN
                     teacher_course tc ON co.id = tc.course_id
                 WHERE
                     tc.teacher_id = ?
                     AND co.semester_id = ?
                 ORDER BY
                     cl.name ASC;
                """;
        return JdbcUtils.executeQuery(
                sql,
                Class.class,
                teacherId,
                semesterId
        );
    }

    @Override
    public List<Course> getTeacherCourses(Integer teacherId, Integer semesterId) throws SQLException, FileNotFoundException {
        String sql = """
                SELECT DISTINCT
                    c.id,
                    c.name
                FROM
                    course c
                JOIN
                    teacher_course tc ON c.id = tc.course_id
                JOIN
                    semester s ON c.semester_id = s.id
                WHERE
                    tc.teacher_id = ?
                    AND c.semester_id = ?
                ORDER BY
                    c.name ASC;
                """;
        return JdbcUtils.executeQuery(
                sql,
                Course.class,
                teacherId,
                semesterId
        );
    }

    @Override
    public List<Semester> getTeacherSemesters(Integer teacherId) throws SQLException, FileNotFoundException {
        String sql = """
                SELECT DISTINCT
                    s.id,
                    s.name,
                    s.start_time,
                    s.end_time
                FROM
                    semester s
                JOIN
                    course c ON s.id = c.semester_id
                JOIN
                    teacher_course tc ON c.id = tc.course_id
                WHERE
                    tc.teacher_id = ?
                ORDER BY
                    s.start_time DESC;
                """;
        return JdbcUtils.executeQuery(
                sql,
                Semester.class,
                teacherId
        );
    }
}
