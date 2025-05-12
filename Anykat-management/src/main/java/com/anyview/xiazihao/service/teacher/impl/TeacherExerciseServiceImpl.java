package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherExerciseDao;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherExercise;
import com.anyview.xiazihao.service.teacher.TeacherExerciseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherExerciseServiceImpl implements TeacherExerciseService {
    @KatAutowired
    private TeacherExerciseDao teacherExerciseDao;

    @Override
    public PageResult<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = teacherExerciseDao.selectTeacherExerciseCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<TeacherExercise> exercises = teacherExerciseDao.selectTeacherExercisesByPage(param);
        return new PageResult<>(total, exercises);
    }
}
