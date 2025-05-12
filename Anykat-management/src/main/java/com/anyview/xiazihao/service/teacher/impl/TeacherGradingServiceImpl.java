package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherGradingDao;
import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestionDetails;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestions;
import com.anyview.xiazihao.service.teacher.TeacherGradingService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherGradingServiceImpl implements TeacherGradingService{
    @KatAutowired
    private TeacherGradingDao teacherGradingDao;

    @Override
    public PageResult<TeacherGradingDetail> selectGradingDetailsByPage(TeacherGradingDetailQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = teacherGradingDao.selectGradingDetailCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<TeacherGradingDetail> details = teacherGradingDao.selectGradingDetailsByPage(param);
        return new PageResult<>(total, details);
    }

    @Override
    public List<TeacherGradingQuestions> selectGradingQuestions(Integer exerciseId, Integer studentId) throws SQLException, FileNotFoundException {
        return teacherGradingDao.selectGradingQuestions(exerciseId, studentId);
    }

    @Override
    public TeacherGradingQuestionDetails selectGradingQuestionDetails(Integer exerciseId, Integer studentId, Integer questionId) throws SQLException, FileNotFoundException {
        return teacherGradingDao.selectGradingQuestionDetails(exerciseId, studentId, questionId);
    }
}
