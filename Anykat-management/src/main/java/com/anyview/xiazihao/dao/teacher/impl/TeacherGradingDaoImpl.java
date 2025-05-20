package com.anyview.xiazihao.dao.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherGradingDao;
import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestionDetails;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestions;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherGradingDaoImpl implements TeacherGradingDao {
    @Override
    public Integer selectGradingDetailCount(TeacherGradingDetailQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT COUNT(*)
        FROM v_teacher_grading_details
        WHERE teacher_id = #{teacherId}
            AND exercise_id = #{exerciseId}
            AND (#{classNames} IS NULL OR class_names LIKE CONCAT('%', #{classNames}, '%'))
            AND (#{studentId} IS NULL OR student_id = #{studentId})
            AND (#{studentName} IS NULL OR student_name LIKE CONCAT('%', #{studentName}, '%'))
        """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<TeacherGradingDetail> selectGradingDetailsByPage(TeacherGradingDetailQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM v_teacher_grading_details
        WHERE teacher_id = #{teacherId}
            AND exercise_id = #{exerciseId}
            AND (#{classNames} IS NULL OR class_names LIKE CONCAT('%', #{classNames}, '%'))
            AND (#{studentId} IS NULL OR student_id = #{studentId})
            AND (#{studentName} IS NULL OR student_name LIKE CONCAT('%', #{studentName}, '%'))
        ORDER BY last_submit_time DESC
        LIMIT #{pageSize} OFFSET #{offset}
        """;
        return JdbcUtils.executeQuery(
                sql,
                TeacherGradingDetail.class,
                param
        );
    }

    @Override
    public List<TeacherGradingQuestions> selectGradingQuestions(Integer exerciseId, Integer studentId) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM v_teacher_grading_questions
        WHERE exercise_id = ?
            AND student_id = ?
        """;
        return JdbcUtils.executeQuery(
                sql,
                TeacherGradingQuestions.class,
               exerciseId,
                 studentId
        );
    }

    @Override
    public TeacherGradingQuestionDetails selectGradingQuestionDetails(Integer exerciseId, Integer studentId, Integer questionId) throws SQLException, FileNotFoundException {
        String sql = """
        SELECT *
        FROM v_teacher_grading_question_details
        WHERE exercise_id = ?
            AND student_id = ?
            AND question_id = ?
        """;
        List<TeacherGradingQuestionDetails> details = JdbcUtils.executeQuery(
                sql,
                TeacherGradingQuestionDetails.class,
                exerciseId,
                studentId,
                questionId
        );
        return details.isEmpty() ? null : details.get(0);
    }

    @Override
    public void updateStudentAnswerCorrection(StudentAnswer studentAnswer) throws SQLException, FileNotFoundException {
        String sql = """
            UPDATE student_answer
            SET correct_status = ?,
                correct_comment = ?,
                correct_time = ?,
                score = ?
            WHERE id = ?
            """;
        JdbcUtils.executeUpdate(
                sql,
                studentAnswer.getCorrectStatus(),
                studentAnswer.getCorrectComment(),
                studentAnswer.getCorrectTime(),
                studentAnswer.getScore(),
                studentAnswer.getId()
        );
    }

    @Override
    public StudentAnswer selectStudentAnswerById(Integer id) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM student_answer
            WHERE id = ?
            """;
       List<StudentAnswer> answers = JdbcUtils.executeQuery(
                sql,
                StudentAnswer.class,
                id
        );
       return answers.isEmpty() ? null : answers.get(0);
    }
}
