package com.anyview.xiazihao.service.student.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentCourseDao;
import com.anyview.xiazihao.entity.param.view.StudentCourseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.StudentCourseProgress;
import com.anyview.xiazihao.entity.view.StudentCourseView;
import com.anyview.xiazihao.service.student.StudentCourseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class StudentCourseServiceImpl implements StudentCourseService {
    @KatAutowired
    private StudentCourseDao studentCourseDao;

    @Override
    public PageResult<StudentCourseView> selectStudentCoursesByPage(StudentCourseQueryParam param) throws SQLException, FileNotFoundException {
        // 设置分页偏移量
        param.setOffset((param.getPage() - 1) * param.getPageSize());

        // 查询总数
        Integer total = studentCourseDao.selectStudentCourseCount(param);

        // 查询分页数据
        List<StudentCourseView> studentCourses = studentCourseDao.selectStudentCoursesByPage(param);

        return new PageResult<>(total, studentCourses);
    }

    @Override
    public StudentCourseProgress selectStudentCourseProgress(Integer studentId, Integer courseId, Integer exerciseStatus) throws SQLException, FileNotFoundException {
        return studentCourseDao.selectStudentCourseProgress(studentId, courseId, exerciseStatus);
    }
}
