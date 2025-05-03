package com.anyview.xiazihao.aspectProcessor;

import java.lang.reflect.Method;

// 通知包装类
public record AdviceWrapper(Object aspectInstance, Method adviceMethod, int priority) {
    public AdviceWrapper(Object aspectInstance, Method adviceMethod, int priority) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.priority = priority;
        this.adviceMethod.setAccessible(true);
    }

    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        return adviceMethod.invoke(aspectInstance, joinPoint);
    }
}
