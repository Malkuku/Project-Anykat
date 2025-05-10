package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.CourseQueryParam;
import com.anyview.xiazihao.entity.pojo.Course;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface CourseDao {
    //查询课程数量
    Integer selectCourseCount(CourseQueryParam param) throws SQLException, FileNotFoundException;
    //查询课程分页
    List<Course> selectCourseByPage(CourseQueryParam param) throws SQLException, FileNotFoundException;
    //查询课程
    Course selectCourseById(Integer id) throws SQLException, FileNotFoundException;
}
