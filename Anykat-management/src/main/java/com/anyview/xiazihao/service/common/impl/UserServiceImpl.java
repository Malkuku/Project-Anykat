package com.anyview.xiazihao.service.common.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.common.UserDao;
import com.anyview.xiazihao.entity.exception.NoDatabaseContentException;
import com.anyview.xiazihao.entity.exception.PermissionDeniedException;
import com.anyview.xiazihao.entity.param.pojo.UserQueryParam;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.common.UserService;
import com.anyview.xiazihao.utils.JwtUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public User login(String username, String password, Integer role) throws SQLException, FileNotFoundException {
        //验证信息
        User user = userDao.selectUserByUsername(username);
        if(user == null || !user.getPassword().equals(password)) {
            throw new NoDatabaseContentException("用户名或密码错误");
        }
        if(!Objects.equals(user.getRole(), role)){
            throw new PermissionDeniedException("身份验证错误");
        }
        //计算token
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("password", user.getPassword());
        map.put("role", user.getRole());
        String token = JwtUtils.createToken(map);
        user.setToken(token);

        //如果是管理员，则计算管理员token
        if(user.getRole() == 2) {
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("password", user.getPassword());
            map.put("role", user.getRole());
            String adminToken = JwtUtils.createAdminToken(map);
            user.setAdminToken(adminToken);
        }

        return user;
    }
}
