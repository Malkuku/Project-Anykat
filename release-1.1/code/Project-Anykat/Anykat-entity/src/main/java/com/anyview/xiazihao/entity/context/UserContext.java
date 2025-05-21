package com.anyview.xiazihao.entity.context;

import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.entity.pojo.User;
import java.io.FileNotFoundException;

public class UserContext {
    private static final boolean AUTH_OPEN;
    static {
        try {
            AUTH_OPEN = AppConfig.getInstance().getSecurity().isAuthOpen();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ThreadLocal<User> userContext = new ThreadLocal<>();

    public static boolean isAuthOpen() {
        return AUTH_OPEN;
    }

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
