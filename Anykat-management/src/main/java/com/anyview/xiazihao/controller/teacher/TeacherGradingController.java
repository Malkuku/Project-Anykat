package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;
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
        return teacherGradingService.selectGradingDetailsByPage(param);
    }
}
