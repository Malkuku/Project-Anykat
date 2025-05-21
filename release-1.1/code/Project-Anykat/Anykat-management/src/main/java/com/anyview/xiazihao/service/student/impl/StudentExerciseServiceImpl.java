package com.anyview.xiazihao.service.student.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.student.StudentExerciseDao;
import com.anyview.xiazihao.entity.param.view.StudentExerciseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.StudentExerciseView;
import com.anyview.xiazihao.service.student.StudentExerciseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class StudentExerciseServiceImpl implements StudentExerciseService {
    @KatAutowired
    private StudentExerciseDao studentExerciseDao;

    @Override
    @KatTransactional
    public PageResult<StudentExerciseView> selectStudentExercisesByPage(StudentExerciseQueryParam param) throws SQLException, FileNotFoundException {
        // 设置分页偏移量
        param.setOffset((param.getPage() - 1) * param.getPageSize());

        // 查询总数
        Integer total = studentExerciseDao.selectStudentExerciseCount(param);

        // 查询分页数据
        List<StudentExerciseView> studentExercises = studentExerciseDao.selectStudentExercisesByPage(param);

        return new PageResult<>(total, studentExercises);
    }
}
