package com.anyview.xiazihao.controller.common;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatPathVariable;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.pojo.SemesterQueryParam;
import com.anyview.xiazihao.entity.pojo.Semester;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.SemesterService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/semesters")
public class SemesterController {
    @KatAutowired
    private SemesterService semesterService;

    // 分页查询学期信息
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<Semester> selectSemesterByPage(
            @KatRequestParam("param") SemesterQueryParam param) throws SQLException, FileNotFoundException {
        return semesterService.selectSemesterByPage(param);
    }

    // 根据ID查询学期
    @KatRequestMapping(path = "/{id}", method = "GET")
    public Semester selectSemesterById(
            @KatPathVariable("id") Integer id) throws SQLException, FileNotFoundException {
        return semesterService.selectSemesterById(id);
    }
}
