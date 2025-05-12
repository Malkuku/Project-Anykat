package com.anyview.xiazihao.service.teacher;

import com.anyview.xiazihao.entity.exception.NoDatabaseContentException;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.pojo.Exercise;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherExercise;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface TeacherExerciseService {
    // 教师练习列表分页查询
    PageResult<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException;

    // 添加练习
    void addExercise(Exercise exercise) throws SQLException, FileNotFoundException;

    // 更新练习状态
    void updateExerciseStatus(Integer id, Integer status) throws NoDatabaseContentException, SQLException, FileNotFoundException;

    //  删除练习
    void deleteExercise(Integer id) throws SQLException, FileNotFoundException;
}
