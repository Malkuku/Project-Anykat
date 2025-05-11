package com.anyview.xiazihao.service.student;

import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface StudentAnswerService {
    // 获取练习题
    List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, SQLException, FileNotFoundException;
}
