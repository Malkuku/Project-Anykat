package com.anyview.xiazihao.dao.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentExerciseDao;
import com.anyview.xiazihao.entity.param.view.StudentExerciseQueryParam;
import com.anyview.xiazihao.entity.view.StudentExerciseView;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class StudentExerciseDaoImpl implements StudentExerciseDao {
    @Override
    public Integer selectStudentExerciseCount(StudentExerciseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT COUNT(*)
        FROM v_student_exercises
        WHERE course_id = #{courseId}
            AND student_id = #{studentId}
            AND (#{exerciseName} IS NULL OR exercise_name LIKE CONCAT('%', #{exerciseName}, '%'))
            AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
            AND (#{semesterName} IS NULL OR semester_name LIKE CONCAT('%', #{semesterName}, '%'))
            AND (#{minTotalScore} IS NULL OR total_exercise_score >= #{minTotalScore})
            AND (#{maxTotalScore} IS NULL OR total_exercise_score <= #{maxTotalScore})
            AND (#{minStudentScore} IS NULL OR student_total_score >= #{minStudentScore})
            AND (#{maxStudentScore} IS NULL OR student_total_score <= #{maxStudentScore})
            AND (#{status} IS NULL OR status = #{status})
            AND (#{startTimeBegin} IS NULL OR start_time >= #{startTimeBegin})
            AND (#{startTimeEnd} IS NULL OR start_time <= #{startTimeEnd})
            AND (#{endTimeBegin} IS NULL OR end_time >= #{endTimeBegin})
            AND (#{endTimeEnd} IS NULL OR end_time <= #{endTimeEnd})
        """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<StudentExerciseView> selectStudentExercisesByPage(StudentExerciseQueryParam param) throws SQLException, SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM v_student_exercises
        WHERE course_id = #{courseId}
            AND student_id = #{studentId}
            AND (#{exerciseName} IS NULL OR exercise_name LIKE CONCAT('%', #{exerciseName}, '%'))
            AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
            AND (#{semesterName} IS NULL OR semester_name LIKE CONCAT('%', #{semesterName}, '%'))
            AND (#{minTotalScore} IS NULL OR total_exercise_score >= #{minTotalScore})
            AND (#{maxTotalScore} IS NULL OR total_exercise_score <= #{maxTotalScore})
            AND (#{minStudentScore} IS NULL OR student_total_score >= #{minStudentScore})
            AND (#{maxStudentScore} IS NULL OR student_total_score <= #{maxStudentScore})
            AND (#{status} IS NULL OR status = #{status})
            AND (#{startTimeBegin} IS NULL OR start_time >= #{startTimeBegin})
            AND (#{startTimeEnd} IS NULL OR start_time <= #{startTimeEnd})
            AND (#{endTimeBegin} IS NULL OR end_time >= #{endTimeBegin})
            AND (#{endTimeEnd} IS NULL OR end_time <= #{endTimeEnd})
        ORDER BY start_time DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                StudentExerciseView.class,
                param
        );
    }
}
