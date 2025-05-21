package com.anyview.xiazihao.controller.common;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestBody;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.param.pojo.UserQueryParam;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.UserService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/common-users")
public class UserController {
    @KatAutowired
    private UserService userService;

    //用户登录
    @KatRequestMapping(path = "/login", method = "POST")
    public User login(
            @KatRequestBody User user) throws SQLException, FileNotFoundException {
        return userService.login(user.getUsername(), user.getPassword(),  user.getRole());
    }


    // 条件分页查询用户信息
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<User> selectUserByPage(
            @KatRequestParam("param") UserQueryParam param) throws SQLException, FileNotFoundException {
        return userService.selectUserByPage(param);
    }
}
