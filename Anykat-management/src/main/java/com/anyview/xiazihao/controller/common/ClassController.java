package com.anyview.xiazihao.controller.common;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatPathVariable;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.ClassQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.ClassService;
import com.anyview.xiazihao.entity.pojo.Class;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/classes")
public class ClassController {
    @KatAutowired
    private ClassService classService;

    // 分页查询班级信息
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<Class> selectClassByPage(
            @KatRequestParam("param") ClassQueryParam param) throws SQLException, FileNotFoundException {
        return classService.selectClassByPage(param);
    }

    // 根据ID查询班级
    @KatRequestMapping(path = "/{id}", method = "GET")
    public Class selectClassById(
            @KatPathVariable("id") Integer id) throws SQLException, FileNotFoundException {
        return classService.selectClassById(id);
    }

}
