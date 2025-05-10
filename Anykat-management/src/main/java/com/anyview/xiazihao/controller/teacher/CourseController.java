package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatPathVariable;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.CourseQueryParam;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.teacher.CourseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/courses")
public class CourseController {
    @KatAutowired
    private CourseService courseService;

    // 分页查询课程信息
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<Course> selectCourseByPage(
            @KatRequestParam("param") CourseQueryParam param) throws SQLException, FileNotFoundException {
        return courseService.selectCourseByPage(param);
    }

    // 根据ID查询课程
    @KatRequestMapping(path = "/{id}", method = "GET")
    public Course selectCourseById(
            @KatPathVariable("id") Integer id) throws SQLException, FileNotFoundException {
        return courseService.selectCourseById(id);
    }
}
