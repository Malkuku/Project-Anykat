package com.anyview.xiazihao.controller.student;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.context.UserContext;
import com.anyview.xiazihao.entity.param.view.StudentCourseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.StudentCourseView;
import com.anyview.xiazihao.service.student.StudentCourseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/student-courses")
public class StudentCourseController {
    @KatAutowired
    private StudentCourseService studentCourseService;

    // 学生课程信息分页查询
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<StudentCourseView> selectStudentCoursesByPage(
            @KatRequestParam("studentId") Integer studentId,
            @KatRequestParam(value = "courseName", required = false) String courseName,
            @KatRequestParam(value = "semesterId", required = false) Integer semesterId,
            @KatRequestParam(value = "page", defaultValue = "1") Integer page,
            @KatRequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) throws SQLException, FileNotFoundException {
        if(UserContext.isAuthOpen()){
            try{
                studentId = UserContext.getUser().getId();
            }finally{
                UserContext.clear();
            }
        }

        StudentCourseQueryParam param = new StudentCourseQueryParam();
        param.setStudentId(studentId);
        param.setCourseName(courseName);
        param.setSemesterId(semesterId);
        param.setPage(page);
        param.setPageSize(pageSize);

        return studentCourseService.selectStudentCoursesByPage(param);
    }
}
