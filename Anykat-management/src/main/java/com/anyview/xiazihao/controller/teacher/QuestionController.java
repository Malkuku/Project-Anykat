package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.*;
import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.pojo.question.ChoiceQuestion;
import com.anyview.xiazihao.entity.pojo.question.SubjectiveQuestion;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.teacher.QuestionService;
import com.anyview.xiazihao.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@Slf4j
@KatComponent
@KatController
@KatRequestMapping(path = "/questions")
public class QuestionController {
    @KatAutowired
    private QuestionService questionService;

    //修改简答题信息
    @KatRequestMapping(path = "/subjective", method = "PUT")
    public void updateSubjectiveQuestion(
            @KatRequestBody SubjectiveQuestion question) throws SQLException, FileNotFoundException {
        questionService.updateSubjectiveQuestion(question);
    }

    //添加简答题信息
    @KatRequestMapping(path = "/subjective", method = "POST")
    public void addSubjectiveQuestion(
            @KatRequestBody SubjectiveQuestion question) throws SQLException, FileNotFoundException {
        questionService.addSubjectiveQuestion(question);
    }

    //根据题目ID查询简答题信息
    @KatRequestMapping(path = "/subjective/{questionId}", method = "GET")
    public SubjectiveQuestion selectSubjectiveQuestionByQuestionId(
            @KatPathVariable("questionId") Integer questionId) throws SQLException, FileNotFoundException {
        return questionService.selectSubjectiveQuestionByQuestionId(questionId);
    }

    //修改选择题信息
    @KatRequestMapping(path = "/choice", method = "PUT")
    public void updateChoiceQuestion(
            @KatRequestBody ChoiceQuestion question) throws SQLException, FileNotFoundException {
        questionService.updateChoiceQuestion(question);
    }

    //添加选择题信息
    @KatRequestMapping(path = "/choice", method = "POST")
    public void addChoiceQuestion(
            @KatRequestBody ChoiceQuestion question) throws SQLException, FileNotFoundException {
        questionService.addChoiceQuestion(question);
    }

    //根据题目ID查询选择题信息
    @KatRequestMapping(path = "/choice/{questionId}", method = "GET")
    public ChoiceQuestion selectChoiceQuestionByQuestionId(
            @KatPathVariable("questionId") Integer questionId) throws SQLException, FileNotFoundException {
        return questionService.selectChoiceQuestionByQuestionId(questionId);
    }

    //根据ID查询题目
    @KatRequestMapping(path = "/{id}", method = "GET")
    public BaseQuestion selectQuestionById(
            @KatPathVariable("id") Integer id) throws SQLException, FileNotFoundException {
        return questionService.selectQuestionById(id);
    }

    //修改题目信息
    @KatRequestMapping(path = "", method = "PUT")
    public void updateQuestion(
            @KatRequestBody BaseQuestion question) throws SQLException, FileNotFoundException {
        questionService.updateQuestion(question);
    }

    //添加题目
    @KatRequestMapping(path = "", method = "POST")
    public void addQuestion(
            @KatRequestBody BaseQuestion question) throws SQLException, FileNotFoundException {
        log.debug("接收到的题目信息:{}", question);
        questionService.addQuestion(question);
    }

    //批量删除题目
    @KatRequestMapping(path = "", method = "DELETE")
    public void deleteQuestionsByIds(
            @KatRequestParam("ids") String ids) throws SQLException, FileNotFoundException {
        log.debug("ids: {}", (Object) ids.split(","));
        questionService.deleteQuestionsByIds(ServletUtils.parseStringToList(ids, Integer.class));
    }

    //条件分页查询问题信息
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<BaseQuestion> selectQuestionByPage(
            @KatRequestParam("param") QuestionQueryParam param) throws SQLException, FileNotFoundException {
        return questionService.selectQuestionByPage(param);
    }
}
