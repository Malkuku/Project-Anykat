package com.anyview.xiazihao.dao.student;

import com.anyview.xiazihao.entity.param.view.StudentCourseQueryParam;
import com.anyview.xiazihao.entity.view.StudentCourseView;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface StudentCourseDao {
    // 根据条件查询学生课程总数
    Integer selectStudentCourseCount(StudentCourseQueryParam param) throws FileNotFoundException, SQLException;
    //  根据条件查询学生课程
    List<StudentCourseView> selectStudentCoursesByPage(StudentCourseQueryParam param) throws SQLException, FileNotFoundException;
}
