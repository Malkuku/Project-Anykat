package com.anyview.xiazihao.service.student.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentAnswerDao;
import com.anyview.xiazihao.entity.exception.IncompleteParameterException;
import com.anyview.xiazihao.entity.exception.PermissionDeniedException;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.pojo.question.ChoiceQuestion;
import com.anyview.xiazihao.entity.view.StudentExerciseQuestion;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestionDetails;
import com.anyview.xiazihao.service.student.StudentAnswerService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@KatComponent
@KatSingleton
public class StudentAnswerServiceImpl implements StudentAnswerService {
    @KatAutowired
    private StudentAnswerDao studentAnswerDao;

    @Override
    @KatTransactional
    public List<StudentExerciseQuestion> selectExerciseQuestions(Integer exerciseId, Integer studentId, Integer courseId) throws SQLException, FileNotFoundException {
        List<StudentExerciseQuestion> questions = studentAnswerDao.selectExerciseQuestions(exerciseId, studentId, courseId);
        //根据studentId查询问题列表
        for (StudentExerciseQuestion question : questions) {
            //根据questionId查询答题记录
            StudentAnswer studentAnswer = studentAnswerDao.selectStudentAnswer(studentId, exerciseId, question.getQuestionId());
            if(studentAnswer != null) question.setStudentAnswer(studentAnswer);
        }
        return questions;
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
        //检查对应练习的状态
        //统计练习id
        Set<Integer> exerciseIds = new HashSet<>();
        for (StudentAnswer answer : answers) {
           exerciseIds.add(answer.getExerciseId());
        }
        for (Integer exerciseId : exerciseIds) {
            Integer exerciseStatus = studentAnswerDao.selectExerciseStatus(exerciseId);
            if(exerciseStatus == null) throw new IncompleteParameterException("练习不存在");
            if(exerciseStatus == 0) throw new PermissionDeniedException("练习未开始");
            if(exerciseStatus == 2) throw new PermissionDeniedException("练习已结束");
        }

        //遍历提交
        for (StudentAnswer answer : answers) {
            //尝试查询记录
            StudentAnswer oldAnswer = studentAnswerDao.selectStudentAnswer(answer.getStudentId(), answer.getExerciseId(), answer.getQuestionId());
            //如果是选择题，且提交了答案，则自动批改选择题
            if(answer.getCorrectStatus() == 1 && (oldAnswer == null || oldAnswer.getCorrectStatus() == 0)){
                ChoiceQuestion choiceQuestion = studentAnswerDao.selectChoiceQuestion(answer.getQuestionId());
                if(choiceQuestion != null){
                    Integer currentScore = studentAnswerDao.findCurrentScore(answer.getQuestionId());
                    //单选题
                    if (!choiceQuestion.getIsMulti()){
                        if(choiceQuestion.getCorrectAnswer().equals(answer.getAnswer())){
                            answer.setScore(currentScore);
                            answer.setCorrectStatus(2);
                            answer.setCorrectComment("答案正确");
                        }else{
                            answer.setScore(0);
                            answer.setCorrectStatus(2);
                            answer.setCorrectComment("答案错误");
                        }
                    }
                    //多选题
                    else {
                        // 获取正确答案和用户答案
                        String[] correctAnswers = choiceQuestion.getCorrectAnswer().split(",");
                        String[] userAnswers = answer.getAnswer().split(",");

                        // 转换为Set方便比较
                        Set<String> correctSet = new HashSet<>(Arrays.asList(correctAnswers));
                        Set<String> userSet = new HashSet<>(Arrays.asList(userAnswers));

                        // 完全正确
                        if (correctSet.equals(userSet)) {
                            answer.setScore(currentScore);
                            answer.setCorrectStatus(2);
                            answer.setCorrectComment("答案完全正确");
                        }
                        // 部分正确
                        else if (!Collections.disjoint(correctSet, userSet)) {
                            // 计算得分（四舍五入）
                            double score = currentScore * 0.5;
                            answer.setScore((int) Math.round(score));
                            answer.setCorrectStatus(2);
                            answer.setCorrectComment("答案部分正确");
                        }
                        // 完全错误
                        else {
                            answer.setScore(0);
                            answer.setCorrectStatus(2);
                            answer.setCorrectComment("答案错误");
                        }
                    }
                }
            }

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
