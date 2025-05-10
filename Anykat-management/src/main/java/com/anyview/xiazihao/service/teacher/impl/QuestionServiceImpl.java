package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.QuestionDao;
import com.anyview.xiazihao.entity.param.pojo.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.pojo.question.ChoiceQuestion;
import com.anyview.xiazihao.entity.pojo.question.SubjectiveQuestion;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.teacher.QuestionService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class QuestionServiceImpl implements QuestionService {
    @KatAutowired
    private QuestionDao questionDao;

    @Override
    public PageResult<BaseQuestion> selectQuestionByPage(QuestionQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = questionDao.selectQuestionCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<BaseQuestion> questions = questionDao.selectQuestionByPage(param);
        return new PageResult<>(total, questions);
    }

    @Override
    public void deleteQuestionsByIds(List<Integer> ids) throws SQLException, FileNotFoundException {
        questionDao.deleteQuestionsByIds(ids);
    }

    @Override
    public void addQuestion(BaseQuestion question) throws SQLException, FileNotFoundException {
        questionDao.addQuestion(question);
    }

    @Override
    public void updateQuestion(BaseQuestion question) throws SQLException, FileNotFoundException {
        questionDao.updateQuestion(question);
    }

    @Override
    public BaseQuestion selectQuestionById(Integer id) throws SQLException, FileNotFoundException {
        return questionDao.selectQuestionById(id);
    }

    @Override
    public ChoiceQuestion selectChoiceQuestionByQuestionId(Integer questionId) throws SQLException, FileNotFoundException {
        return questionDao.selectChoiceQuestionByQuestionId(questionId);
    }

    @Override
    public void addChoiceQuestion(ChoiceQuestion question) throws SQLException, FileNotFoundException {
        BaseQuestion baseQuestion = questionDao.selectQuestionById(question.getQuestionId());
        if(baseQuestion.getType() != 1 && baseQuestion.getType() != 0) throw new RuntimeException("试题类型错误");
        questionDao.addChoiceQuestion(question);
    }

    @Override
    public void updateChoiceQuestion(ChoiceQuestion question) throws SQLException, FileNotFoundException {
        questionDao.updateChoiceQuestion(question);
    }

    @Override
    public SubjectiveQuestion selectSubjectiveQuestionByQuestionId(Integer questionId) throws SQLException, FileNotFoundException {
        return questionDao.selectSubjectiveQuestionByQuestionId(questionId);
    }

    @Override
    public void addSubjectiveQuestion(SubjectiveQuestion question) throws SQLException, FileNotFoundException {
        BaseQuestion baseQuestion = questionDao.selectQuestionById(question.getQuestionId());
        if(baseQuestion.getType() != 2) throw new RuntimeException("试题类型错误");
        questionDao.addSubjectiveQuestion(question);
    }

    @Override
    public void updateSubjectiveQuestion(SubjectiveQuestion question) throws SQLException, FileNotFoundException {
        questionDao.updateSubjectiveQuestion(question);
    }
}
