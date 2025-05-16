package com.anyview.xiazihao.service.teacher;

import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.pojo.Course;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface TeacherLinkService {
    // 获取老师所教授的班级
    List<Class> getTeacherClasses(Integer teacherId) throws SQLException, FileNotFoundException;
    // 获取老师所教授的课程
    List<Course> getTeacherCourses(Integer teacherId, Integer semesterId) throws SQLException, FileNotFoundException;
}
