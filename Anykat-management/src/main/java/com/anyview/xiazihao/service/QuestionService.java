package com.anyview.xiazihao.service;

import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.result.PageResult;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface QuestionService {
    //  分页查询
    PageResult<BaseQuestion> selectQuestionByPage(QuestionQueryParam param) throws SQLException, FileNotFoundException;

    //  删除
    void deleteQuestionsByIds(List<Integer> ids) throws SQLException, FileNotFoundException;
}
