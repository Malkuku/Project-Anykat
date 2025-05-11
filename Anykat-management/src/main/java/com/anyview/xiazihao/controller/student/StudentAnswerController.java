package com.anyview.xiazihao.controller.student;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;
import com.anyview.xiazihao.service.student.StudentAnswerService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatController
@KatRequestMapping(path = "/student-answers")
public class StudentAnswerController {
    @KatAutowired
    private StudentAnswerService studentAnswerService;

    // 练习题目查询
    @KatRequestMapping(path = "/questions", method = "GET")
    public List<StudentExerciseQuestion> getExerciseQuestions(
            @KatRequestParam("exerciseId") Integer exerciseId,
            @KatRequestParam("studentId") Integer studentId,
            @KatRequestParam("courseId") Integer courseId) throws SQLException, FileNotFoundException {
        return studentAnswerService.selectExerciseQuestions(exerciseId, studentId, courseId);
    }
}
