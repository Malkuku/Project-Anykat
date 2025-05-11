package com.anyview.xiazihao.service.student;

import com.anyview.xiazihao.entity.param.view.StudentCourseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.StudentCourseView;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface StudentCourseService {
    //  查询学生课程
    PageResult<StudentCourseView> selectStudentCoursesByPage(StudentCourseQueryParam param) throws SQLException, FileNotFoundException;
}
