package com.anyview.xiazihao.service.common;

import com.anyview.xiazihao.entity.param.CourseQueryParam;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.entity.result.PageResult;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface CourseService {
    // 分页查询课程
    PageResult<Course> selectCourseByPage(CourseQueryParam param) throws SQLException, FileNotFoundException;
    // 根据id查询课程
    Course selectCourseById(Integer id) throws SQLException, FileNotFoundException;
}
