package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.SemesterDao;
import com.anyview.xiazihao.entity.param.SemesterQueryParam;
import com.anyview.xiazihao.entity.pojo.Semester;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.teacher.SemesterService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class SemesterServiceImpl implements SemesterService {
    @KatAutowired
    private SemesterDao semesterDao;

    @Override
    public PageResult<Semester> selectSemesterByPage(SemesterQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = semesterDao.selectSemesterCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<Semester> semesters = semesterDao.selectSemesterByPage(param);
        return new PageResult<>(total, semesters);
    }

    @Override
    public Semester selectSemesterById(Integer id) throws SQLException, FileNotFoundException {
        return semesterDao.selectSemesterById(id);
    }
}
