package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.SemesterQueryParam;
import com.anyview.xiazihao.entity.pojo.Semester;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface SemesterDao {
    // 查询学期数量
    Integer selectSemesterCount(SemesterQueryParam param) throws SQLException, FileNotFoundException;
    // 分页查询学期
    List<Semester> selectSemesterByPage(SemesterQueryParam param) throws SQLException, FileNotFoundException;
    // 根据id查询学期
    Semester selectSemesterById(Integer id) throws SQLException, FileNotFoundException;
}
