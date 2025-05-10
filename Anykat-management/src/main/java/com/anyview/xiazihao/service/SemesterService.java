package com.anyview.xiazihao.service;

import com.anyview.xiazihao.entity.param.SemesterQueryParam;
import com.anyview.xiazihao.entity.pojo.Semester;
import com.anyview.xiazihao.entity.result.PageResult;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface SemesterService {
    // 分页查询
    PageResult<Semester> selectSemesterByPage(SemesterQueryParam param) throws SQLException, FileNotFoundException;
    // 根据id查询
    Semester selectSemesterById(Integer id) throws SQLException, FileNotFoundException;
}
