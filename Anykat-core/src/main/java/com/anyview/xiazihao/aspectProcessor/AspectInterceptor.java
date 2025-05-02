package com.anyview.xiazihao.aspectProcessor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class AspectInterceptor {
    private final Object target;
    private final AspectProcessor aspectProcessor;

    public AspectInterceptor(Object target, AspectProcessor aspectProcessor) {
        this.target = target;
        this.aspectProcessor = aspectProcessor;
    }

    //在运行时，拦截目标方法，并执行切面逻辑
    @RuntimeType
    public Object intercept(@Origin Method method,
                            @AllArguments Object[] args,
                            @SuperCall Callable<?> callable) throws Exception {
        try {
            return aspectProcessor.applyAspects(target, method, args);
        } catch (Throwable throwable) {
            if (throwable instanceof Exception) {
                throw (Exception) throwable;
            }
            throw new Exception(throwable);
        }
    }
}
