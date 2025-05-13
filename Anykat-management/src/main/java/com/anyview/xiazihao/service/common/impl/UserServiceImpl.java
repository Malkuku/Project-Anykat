package com.anyview.xiazihao.service.common.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.common.UserDao;
import com.anyview.xiazihao.entity.param.pojo.UserQueryParam;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.UserService;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class UserServiceImpl implements UserService {
    @KatAutowired
    private UserDao userDao;

    @Override
    public PageResult<User> selectUserByPage(UserQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = userDao.selectUserCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<User> users = userDao.selectUserByPage(param);
        return new PageResult<>(total, users);
    }
}
