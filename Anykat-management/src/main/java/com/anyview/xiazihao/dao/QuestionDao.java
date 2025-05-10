package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface QuestionDao {
    Integer selectQuestionCount(QuestionQueryParam param) throws SQLException, FileNotFoundException;

    List<BaseQuestion> selectQuestionByPage(QuestionQueryParam param) throws SQLException, FileNotFoundException;
}
