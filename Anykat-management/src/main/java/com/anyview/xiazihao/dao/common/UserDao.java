package com.anyview.xiazihao.dao.common;

import com.anyview.xiazihao.entity.param.pojo.UserQueryParam;
import com.anyview.xiazihao.entity.pojo.User;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    // 查询用户总数
    Integer selectUserCount(UserQueryParam param) throws SQLException, FileNotFoundException, SQLException;
    //  分页查询用户
    List<User> selectUserByPage(UserQueryParam param) throws SQLException, FileNotFoundException;
    //  根据用户名查询用户
    User selectUserByUsername(String username) throws SQLException, FileNotFoundException;
}
