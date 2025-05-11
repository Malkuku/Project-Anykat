package com.anyview.xiazihao.dao.student;

import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface StudentAnswerDao {
    // 根据 exerciseId, studentId, courseId 查询练习题目
    List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, FileNotFoundException;

    // 查询学生答案
    StudentAnswer selectStudentAnswer(Integer studentId, Integer exerciseId, Integer questionId) throws FileNotFoundException, SQLException;

    // 插入学生答案
    void insertStudentAnswer(StudentAnswer answer) throws SQLException, FileNotFoundException;

    // 更新学生答案
    void updateStudentAnswer(StudentAnswer answer) throws SQLException, FileNotFoundException;
}
