package com.anyview.xiazihao.service.common.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.common.ClassDao;
import com.anyview.xiazihao.entity.param.pojo.ClassQueryParam;
import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.ClassService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class ClassServiceImpl implements ClassService {
    @KatAutowired
    private ClassDao classDao;

    @Override
    @KatTransactional
    public PageResult<Class> selectClassByPage(ClassQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = classDao.selectClassCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<Class> classes = classDao.selectClassByPage(param);
        return new PageResult<>(total, classes);
    }

    @Override
    public Class selectClassById(Integer id) throws SQLException, FileNotFoundException {
        return classDao.selectClassById(id);
    }
}
