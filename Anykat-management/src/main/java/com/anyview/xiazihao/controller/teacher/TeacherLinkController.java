package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatPathVariable;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.context.UserContext;
import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.service.teacher.TeacherLinkService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatController
@KatRequestMapping(path = "/teacher-link")
public class TeacherLinkController {
    @KatAutowired
    private TeacherLinkService teacherLinkService;

    // 根据教师ID查询关联班级
    @KatRequestMapping(path = "/classes/{id}", method = "GET")
    public List<Class> getTeacherClasses(
            @KatPathVariable("id") Integer teacherId) throws SQLException, FileNotFoundException {
        if(UserContext.isAuthOpen()){
            try{
                teacherId = UserContext.getUser().getId();
            }finally{
                UserContext.clear();
            }
        }
        return teacherLinkService.getTeacherClasses(teacherId);
    }

    // 根据教师ID查询关联课程
    @KatRequestMapping(path = "/courses", method = "GET")
    public List<Course> getTeacherCourses(
            @KatRequestParam("teacherId") Integer teacherId,
            @KatRequestParam("semesterId") Integer semesterId) throws SQLException, FileNotFoundException {
        if(UserContext.isAuthOpen()){
            try{
                teacherId = UserContext.getUser().getId();
            }finally{
                UserContext.clear();
            }
        }
        return teacherLinkService.getTeacherCourses(teacherId,semesterId);
    }
}
