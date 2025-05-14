package com.anyview.xiazihao.entity.context;

import com.anyview.xiazihao.entity.pojo.User;


public class AdminContext extends UserContext{
    private static final ThreadLocal<User> adminUserContext = new ThreadLocal<>();

    public static void setUser(User user) {
        adminUserContext.set(user);
    }

    public static User getUser() {
        return adminUserContext.get();
    }

    public static void clear() {
        adminUserContext.remove();
    }
}
