package com.anyview.xiazihao.aspectProcessor;


import java.lang.reflect.Method;

public interface ProceedingJoinPoint {
    // 执行链式调用（关键方法）
    Object proceed() throws Throwable;

    // 获取目标方法
    Method getMethod();

    // 获取目标对象
    Object getTarget();

    // 获取方法参数
    Object[] getArgs();

    // 获取方法签名（可选）
    String getSignature();
}
