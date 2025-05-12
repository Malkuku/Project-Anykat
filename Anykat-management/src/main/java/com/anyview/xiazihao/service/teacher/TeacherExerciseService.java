package com.anyview.xiazihao.service.teacher;

import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherExercise;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface TeacherExerciseService {
    // 教师练习列表分页查询
    PageResult<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException;
}
