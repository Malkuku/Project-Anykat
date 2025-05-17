package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherGradingDao;
import com.anyview.xiazihao.entity.exception.IncompleteParameterException;
import com.anyview.xiazihao.entity.exception.NoDatabaseContentException;
import com.anyview.xiazihao.entity.exception.PermissionDeniedException;
import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestionDetails;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestions;
import com.anyview.xiazihao.service.teacher.TeacherGradingService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherGradingServiceImpl implements TeacherGradingService{
    @KatAutowired
    private TeacherGradingDao teacherGradingDao;

    @Override
    @KatTransactional
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

    @Override
    @KatTransactional
    public void updateStudentAnswerCorrection(StudentAnswer studentAnswer) throws SQLException, FileNotFoundException {
        if(studentAnswer.getCorrectStatus() == 0) throw new PermissionDeniedException("没有操作权限");
        StudentAnswer oldStudentAnswer = teacherGradingDao.selectStudentAnswerById(studentAnswer.getId());
        if(oldStudentAnswer == null) throw new NoDatabaseContentException("id不存在");
        if(oldStudentAnswer.getCorrectStatus() == 0) throw new PermissionDeniedException("无法批改未提交答案");
        studentAnswer.setCorrectTime(LocalDateTime.now());
        teacherGradingDao.updateStudentAnswerCorrection(studentAnswer);
    }
}
