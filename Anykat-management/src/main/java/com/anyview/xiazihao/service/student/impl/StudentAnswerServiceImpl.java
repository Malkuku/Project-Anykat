package com.anyview.xiazihao.service.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentAnswerDao;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;
import com.anyview.xiazihao.service.student.StudentAnswerService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class StudentAnswerServiceImpl implements StudentAnswerService {
    @KatAutowired
    private StudentAnswerDao studentAnswerDao;

    @Override
    public List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, FileNotFoundException {
        return studentAnswerDao.selectExerciseQuestions(exerciseId, studentId, courseId);
    }

}
