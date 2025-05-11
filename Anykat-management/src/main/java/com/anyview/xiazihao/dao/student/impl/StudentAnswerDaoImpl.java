package com.anyview.xiazihao.dao.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentAnswerDao;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class StudentAnswerDaoImpl implements StudentAnswerDao {

    @Override
    public List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM v_student_exercise_questions
        WHERE exercise_id = ?
            AND (student_id = ? OR student_id IS NULL)
            AND course_id = ?
        ORDER BY sort_order
        """;
        return JdbcUtils.executeQuery(
                sql,
                StudentExerciseQuestion.class,
                exerciseId, studentId, courseId
        );
    }
}
