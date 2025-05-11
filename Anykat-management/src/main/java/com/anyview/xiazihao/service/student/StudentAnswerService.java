package com.anyview.xiazihao.service.student;

import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface StudentAnswerService {
    // 获取练习题
    List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, SQLException, FileNotFoundException;

    // 获取学生答案
    StudentAnswer selectStudentAnswer(Integer studentId, Integer exerciseId, Integer questionId) throws SQLException, FileNotFoundException;

    // 提交学生答案
    void submitStudentAnswers(List<StudentAnswer> answers) throws SQLException, FileNotFoundException;
}
