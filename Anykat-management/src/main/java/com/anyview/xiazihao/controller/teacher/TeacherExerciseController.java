package com.anyview.xiazihao.controller.teacher;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.*;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.pojo.Exercise;
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

    //删除练习
    @KatRequestMapping(path = "/{id}", method = "DELETE")
    public void deleteExercise(
            @KatPathVariable("id") Integer id) throws SQLException, FileNotFoundException {
        teacherExerciseService.deleteExercise(id);
    }

    // 修改练习状态
    @KatRequestMapping(path = "/status", method = "PUT")
    public void updateExerciseStatus(
            @KatRequestBody Exercise exercise) throws SQLException, FileNotFoundException {
        teacherExerciseService.updateExerciseStatus(exercise.getId(), exercise.getStatus());
    }

    // 添加练习
    @KatRequestMapping(path = "", method = "POST")
    public void addExercise(
            @KatRequestBody Exercise exercise) throws SQLException, FileNotFoundException {
        teacherExerciseService.addExercise(exercise);
    }

    // 教师练习列表分页查询
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<TeacherExercise> selectTeacherExercisesByPage(
            @KatRequestParam("param") TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        return teacherExerciseService.selectTeacherExercisesByPage(param);
    }
}
