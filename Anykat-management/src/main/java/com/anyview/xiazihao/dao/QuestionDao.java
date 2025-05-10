package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.pojo.question.ChoiceQuestion;
import com.anyview.xiazihao.entity.pojo.question.SubjectiveQuestion;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface QuestionDao {
    // 查询题目数量
    Integer selectQuestionCount(QuestionQueryParam param) throws SQLException, FileNotFoundException;
    //  查询题目
    List<BaseQuestion> selectQuestionByPage(QuestionQueryParam param) throws SQLException, FileNotFoundException;
    // 删除题目
    void deleteQuestionsByIds(List<Integer> ids) throws SQLException, FileNotFoundException;
    // 添加题目
    void addQuestion(BaseQuestion question) throws SQLException, FileNotFoundException;
    //  更新题目
    void updateQuestion(BaseQuestion question) throws SQLException, FileNotFoundException;
    //  查询题目
    BaseQuestion selectQuestionById(Integer id) throws SQLException, FileNotFoundException;
    // 查询选择题
    ChoiceQuestion selectChoiceQuestionByQuestionId(Integer questionId) throws SQLException, FileNotFoundException;
    // 添加选择题
    void addChoiceQuestion(ChoiceQuestion question) throws SQLException, FileNotFoundException;
    // 更新选择题
    void updateChoiceQuestion(ChoiceQuestion question) throws SQLException, FileNotFoundException;
    //  查询主观题
    SubjectiveQuestion selectSubjectiveQuestionByQuestionId(Integer questionId) throws SQLException, FileNotFoundException;
    // 添加主观题
    void addSubjectiveQuestion(SubjectiveQuestion question) throws SQLException, FileNotFoundException;
}
