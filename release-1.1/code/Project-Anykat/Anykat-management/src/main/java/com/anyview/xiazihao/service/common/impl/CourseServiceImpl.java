package com.anyview.xiazihao.service.common.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.common.CourseDao;
import com.anyview.xiazihao.entity.param.pojo.CourseQueryParam;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.CourseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class CourseServiceImpl implements CourseService {
    @KatAutowired
    private CourseDao courseDao;

    @Override
    @KatTransactional
    public PageResult<Course> selectCourseByPage(CourseQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = courseDao.selectCourseCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<Course> courses = courseDao.selectCourseByPage(param);
        return new PageResult<>(total, courses);
    }

    @Override
    public Course selectCourseById(Integer id) throws SQLException, FileNotFoundException {
        return courseDao.selectCourseById(id);
    }
}
