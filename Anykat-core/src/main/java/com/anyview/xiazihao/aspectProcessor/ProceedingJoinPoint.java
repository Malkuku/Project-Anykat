package com.anyview.xiazihao.aspectProcessor;


import java.lang.reflect.Method;

public record ProceedingJoinPoint(Object target, Method method, Object[] args) {

    public Object proceed() throws Exception {
        return method.invoke(target, args);
    }
}
