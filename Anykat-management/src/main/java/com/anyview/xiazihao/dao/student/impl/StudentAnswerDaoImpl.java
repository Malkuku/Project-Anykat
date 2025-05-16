package com.anyview.xiazihao.dao.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentAnswerDao;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.pojo.question.ChoiceQuestion;
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
                AND course_id = ?
            ORDER BY sort_order
            """;
        return JdbcUtils.executeQuery(
                sql,
                StudentExerciseQuestion.class,
                exerciseId, courseId
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
                answer,correct_status,submit_time,
                correct_time,score,correct_comment
            ) VALUES (#{studentId}, #{exerciseId}, #{questionId},
                    #{answer}, #{correctStatus}, #{submitTime},
                    #{correctTime},#{score},#{correctComment})
            """;
        JdbcUtils.executeUpdate(
                sql,
                answer
        );
    }

    @Override
    public void updateStudentAnswer(StudentAnswer answer) throws SQLException, FileNotFoundException {
        String sql = """
            UPDATE student_answer SET
                correct_status = COALESCE(#{correctStatus},correct_status),
                submit_time = COALESCE(#{submitTime},submit_time),
                correct_time = COALESCE(#{correctTime},correct_time),
                score = COALESCE(#{score},score),
                correct_comment = COALESCE(#{correctComment},correct_comment),
                answer = COALESCE(#{answer},answer)
                WHERE student_id = #{studentId}
                AND exercise_id = #{exerciseId}
                AND question_id = #{questionId}
            """;
        JdbcUtils.executeUpdate(
                sql,
                answer
        );
    }

    @Override
    public ChoiceQuestion selectChoiceQuestion(Integer questionId) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM choice_question
            WHERE question_id = ?
            """;
        List<ChoiceQuestion> questions =JdbcUtils.executeQuery(
                sql,
                ChoiceQuestion.class,
                questionId
        );
        return questions.isEmpty() ? null : questions.get(0);
    }

    @Override
    public Integer findCurrentScore(Integer questionId) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT score
            FROM base_question
            WHERE id = ?
            """;
        return JdbcUtils.executeQuery(
                sql,
                Integer.class,
                questionId
        ).get(0);
    }
}
