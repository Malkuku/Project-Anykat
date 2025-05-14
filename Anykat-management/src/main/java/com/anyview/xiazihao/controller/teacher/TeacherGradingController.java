package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestBody;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.context.UserContext;
import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestionDetails;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestions;
import com.anyview.xiazihao.service.teacher.TeacherGradingService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatController
@KatRequestMapping(path = "/teacher-grading")
public class TeacherGradingController {
    @KatAutowired
    private TeacherGradingService teacherGradingService;

    //修改批改状态
    @KatRequestMapping(path = "/correction", method = "POST")
    public void updateStudentAnswerCorrection(
            @KatRequestBody StudentAnswer studentAnswer) throws SQLException, FileNotFoundException {
        teacherGradingService.updateStudentAnswerCorrection(studentAnswer);
    }

    //学生答题详情查询
    @KatRequestMapping(path = "/question-details", method = "GET")
    public TeacherGradingQuestionDetails selectGradingQuestionDetails(
            @KatRequestParam("exerciseId") Integer exerciseId,
            @KatRequestParam("studentId") Integer studentId,
            @KatRequestParam("questionId") Integer questionId) throws SQLException, FileNotFoundException {
        return teacherGradingService.selectGradingQuestionDetails(exerciseId, studentId, questionId);
    }

    //简单批改信息查询
    @KatRequestMapping(path = "/questions", method = "GET")
    public List<TeacherGradingQuestions> selectGradingQuestions(
            @KatRequestParam("exerciseId") Integer exerciseId,
            @KatRequestParam("studentId") Integer studentId) throws SQLException, FileNotFoundException {
        return teacherGradingService.selectGradingQuestions(exerciseId,  studentId);
    }

    // 批改详情分页查询
    @KatRequestMapping(path = "/details", method = "GET")
    public PageResult<TeacherGradingDetail> selectGradingDetailsByPage(
            @KatRequestParam("param") TeacherGradingDetailQueryParam param) throws SQLException, FileNotFoundException {
        if(UserContext.isAuthOpen()){
            try{
                param.setTeacherId(UserContext.getUser().getId());
            }finally{
                UserContext.clear();
            }
        }
        return teacherGradingService.selectGradingDetailsByPage(param);
    }
}
