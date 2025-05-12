package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherExercise;
import com.anyview.xiazihao.service.teacher.TeacherExerciseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/teacher-exercises")
public class TeacherExerciseController {
    @KatAutowired
    private TeacherExerciseService teacherExerciseService;

    // 教师练习列表分页查询
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<TeacherExercise> selectTeacherExercisesByPage(
            @KatRequestParam("param") TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        return teacherExerciseService.selectTeacherExercisesByPage(param);
    }
}
