package com.anyview.xiazihao.dao.teacher;

import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.view.TeacherExercise;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface TeacherExerciseDao {
    // 教师练习列表统计
    Integer selectTeacherExerciseCount(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException;
    //  教师练习列表分页查询
    List<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, SQLException, FileNotFoundException;
}
