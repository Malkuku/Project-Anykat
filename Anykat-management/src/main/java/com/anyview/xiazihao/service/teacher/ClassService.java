package com.anyview.xiazihao.service.teacher;

import com.anyview.xiazihao.entity.param.ClassQueryParam;
import com.anyview.xiazihao.entity.pojo.Class;
import com.anyview.xiazihao.entity.result.PageResult;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface ClassService {
    //  分页查询
    PageResult<Class> selectClassByPage(ClassQueryParam param) throws SQLException, FileNotFoundException;
    // 根据id查询
    Class selectClassById(Integer id) throws SQLException, FileNotFoundException;
}
