package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.ClassQueryParam;
import com.anyview.xiazihao.entity.pojo.Class;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface ClassDao {
    //  查询班级数量
    Integer selectClassCount(ClassQueryParam param) throws SQLException, FileNotFoundException;
    //  分页查询班级
    List<Class> selectClassByPage(ClassQueryParam param) throws SQLException, FileNotFoundException;
    //   通过id查询班级
    Class selectClassById(Integer id) throws SQLException, FileNotFoundException;
}
