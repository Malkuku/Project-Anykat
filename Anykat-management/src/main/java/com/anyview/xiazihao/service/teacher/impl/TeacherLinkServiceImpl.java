package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherLinkDao;
import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.service.teacher.TeacherLinkService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherLinkServiceImpl implements TeacherLinkService {
    @KatAutowired
    private TeacherLinkDao teacherLinkDao;

    @Override
    public List<Class> getTeacherClasses(Integer teacherId) throws SQLException, FileNotFoundException {
       return teacherLinkDao.getTeacherClasses(teacherId);
    }

    @Override
    public List<Course> getTeacherCourses(Integer teacherId, Integer semesterId) throws SQLException, FileNotFoundException {
        return teacherLinkDao.getTeacherCourses(teacherId,  semesterId);
    }
}
