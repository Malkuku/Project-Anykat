package com.anyview.xiazihao.dao.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherExerciseDao;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.view.TeacherExercise;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherExerciseDaoImpl implements TeacherExerciseDao {
    @Override
    public Integer selectTeacherExerciseCount(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT COUNT(*)
            FROM v_teacher_exercises
            WHERE teacher_id = #{teacherId}
                AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
                AND (#{exerciseName} IS NULL OR exercise_name LIKE CONCAT('%', #{exerciseName}, '%'))
                AND (#{courseName} IS NULL OR course_name LIKE CONCAT('%', #{courseName}, '%'))
                AND (#{className} IS NULL OR class_name LIKE CONCAT('%', #{className}, '%'))
                AND (#{startTime} IS NULL OR start_time >= #{startTime})
                AND (#{endTime} IS NULL OR end_time <= #{endTime})
                AND (#{status} IS NULL OR status = #{status})
                AND (#{minQuestionCount} IS NULL OR question_count >= #{minQuestionCount})
                AND (#{maxQuestionCount} IS NULL OR question_count <= #{maxQuestionCount})
            """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM v_teacher_exercises
            WHERE teacher_id = #{teacherId}
                AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
                AND (#{exerciseName} IS NULL OR exercise_name LIKE CONCAT('%', #{exerciseName}, '%'))
                AND (#{courseName} IS NULL OR course_name LIKE CONCAT('%', #{courseName}, '%'))
                AND (#{className} IS NULL OR class_name LIKE CONCAT('%', #{className}, '%'))
                AND (#{startTime} IS NULL OR start_time >= #{startTime})
                AND (#{endTime} IS NULL OR end_time <= #{endTime})
                AND (#{status} IS NULL OR status = #{status})
                AND (#{minQuestionCount} IS NULL OR question_count >= #{minQuestionCount})
                AND (#{maxQuestionCount} IS NULL OR question_count <= #{maxQuestionCount})
            ORDER BY start_time DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """;
        return JdbcUtils.executeQuery(
                sql,
                TeacherExercise.class,
                param
        );
    }
}
