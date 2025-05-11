package com.anyview.xiazihao.dao.student;

import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface StudentAnswerDao {
    // 根据 exerciseId, studentId, courseId 查询练习题目
    List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, FileNotFoundException;
}
