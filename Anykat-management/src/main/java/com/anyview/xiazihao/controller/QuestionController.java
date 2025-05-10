package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.QuestionService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/questions")
public class QuestionController {
    @KatAutowired
    private QuestionService questionService;

    //条件分页查询问题信息
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<BaseQuestion> selectQuestionByPage(
            @KatRequestParam("param") QuestionQueryParam param) throws SQLException, FileNotFoundException {
            return questionService.selectQuestionByPage(param);
    }
}
