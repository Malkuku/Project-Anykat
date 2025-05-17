package com.anyview.xiazihao.dao.teacher;

import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.pojo.Course;
import com.anyview.xiazihao.entity.pojo.Semester;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface TeacherLinkDao {
    //  获取老师所带班级
    List<Class> getTeacherClasses(Integer teacherId, Integer semesterId) throws SQLException, FileNotFoundException;
    //  获取老师所带课程
    List<Course> getTeacherCourses(Integer teacherId, Integer semesterId) throws SQLException, FileNotFoundException;
    //  获取老师所带学期
    List<Semester> getTeacherSemesters(Integer teacherId) throws SQLException, FileNotFoundException;
}
