package com.anyview.xiazihao.service.student;

import com.anyview.xiazihao.entity.param.view.StudentExerciseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.StudentExerciseView;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface StudentExerciseService {
    // 查询学生练习列表
    PageResult<StudentExerciseView> selectStudentExercisesByPage(StudentExerciseQueryParam param) throws SQLException, FileNotFoundException;
}
