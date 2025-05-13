package com.anyview.xiazihao.service.common;

import com.anyview.xiazihao.entity.param.pojo.UserQueryParam;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.result.PageResult;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface UserService {
    // 分页查询用户信息
    PageResult<User> selectUserByPage(UserQueryParam param) throws SQLException, SQLException, FileNotFoundException;
    //  用户登录
    User login(String username, String password) throws SQLException, FileNotFoundException;
}
