package com.anyview.xiazihao.entity.context;

import com.anyview.xiazihao.entity.pojo.User;

public class UserContext {
    private static final ThreadLocal<User> userContext = new ThreadLocal<>();

    public static void setUser(User user) {
        userContext.set(user);
    }

    public static User getUser() {
        return userContext.get();
    }

    public static void clear() {
        userContext.remove();
    }
}
