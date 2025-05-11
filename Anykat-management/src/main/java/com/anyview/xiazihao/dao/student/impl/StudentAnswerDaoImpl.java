package com.anyview.xiazihao.dao.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentAnswerDao;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
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

    @Override
    public StudentAnswer selectStudentAnswer(Integer studentId, Integer exerciseId, Integer questionId) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM student_answer
            WHERE student_id = ?
                AND exercise_id = ?
                AND question_id = ?
            """;
        List<StudentAnswer> answers = JdbcUtils.executeQuery(
                sql,
                StudentAnswer.class,
                studentId, exerciseId, questionId
        );
        return answers.isEmpty() ? null : answers.get(0);
    }

    @Override
    public void insertStudentAnswer(StudentAnswer answer) throws SQLException, FileNotFoundException {
        String sql = """
            INSERT INTO student_answer (
                student_id, exercise_id, question_id,
                answer,correct_status,submit_time
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;
        JdbcUtils.executeUpdate(
                sql,
                answer.getStudentId(),
                answer.getExerciseId(),
                answer.getQuestionId(),
                answer.getAnswer(),
                answer.getCorrectStatus(),
                answer.getSubmitTime()
        );
    }

    @Override
    public void updateStudentAnswer(StudentAnswer answer) throws SQLException, FileNotFoundException {
        String sql = """
            UPDATE student_answer SET
                answer = ?,
                correct_status = ?,
                WHERE student_id = ?
                AND exercise_id = ?
                AND question_id = ?
            """;
        JdbcUtils.executeUpdate(
                sql,
                answer.getAnswer(),
                answer.getCorrectStatus(),
                answer.getStudentId(),
                answer.getExerciseId(),
                answer.getQuestionId()
        );
    }
}
