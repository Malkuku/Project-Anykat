package com.anyview.xiazihao.service.student.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentAnswerDao;
import com.anyview.xiazihao.entity.exception.IncompleteParameterException;
import com.anyview.xiazihao.entity.exception.PermissionDeniedException;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;
import com.anyview.xiazihao.service.student.StudentAnswerService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
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

    @Override
    public StudentAnswer selectStudentAnswer(Integer studentId, Integer exerciseId, Integer questionId) throws SQLException, FileNotFoundException {
        return studentAnswerDao.selectStudentAnswer(studentId, exerciseId, questionId);
    }

    @Override
    @KatTransactional
    public void submitStudentAnswers(List<StudentAnswer> answers) throws SQLException, FileNotFoundException {
        //检查权限
        for (StudentAnswer answer : answers) {
            if(answer.getCorrectStatus() == null
                || answer.getStudentId() == null
                || answer.getExerciseId() == null
                || answer.getQuestionId() == null){
                throw new IncompleteParameterException("参数缺失："+ answer);
            }
            if (answer.getCorrectStatus() > 1
                    || answer.getCorrectComment() != null
                    || answer.getScore() != null) {
                throw new PermissionDeniedException("没有更改权限");
            }
            //更新提交时间
            answer.setSubmitTime(LocalDateTime.now());
        }
        //遍历提交
        for (StudentAnswer answer : answers) {
            //尝试查询记录
            StudentAnswer oldAnswer = studentAnswerDao.selectStudentAnswer(answer.getStudentId(), answer.getExerciseId(), answer.getQuestionId());
            //如果不存在，插入
            if (oldAnswer == null) {
                studentAnswerDao.insertStudentAnswer(answer);
            } else {
                //如果存在，检查能否更新
                if (oldAnswer.getCorrectStatus() > 0) {
                    throw new PermissionDeniedException("无法修改已提交答案");
                }
                studentAnswerDao.updateStudentAnswer(answer);
            }
        }
    }

}
