package com.anyview.xiazihao.aspectProcessor;


import java.lang.reflect.Method;
import java.util.List;

public class ChainableProceedingJoinPoint implements ProceedingJoinPoint {
    private final Object target;
    private final Method method;
    private final Object[] args;
    private final List<AdviceWrapper> advices;
    private int currentPosition = 0; // 当前执行的切面位置

    public ChainableProceedingJoinPoint(Object target, Method method, Object[] args, List<AdviceWrapper> advices) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.advices = advices;
    }

    @Override
    public Object proceed() throws Throwable {
        if (currentPosition < advices.size()) {
            // 执行下一个切面
            AdviceWrapper currentAdvice = advices.get(currentPosition++);
            return currentAdvice.invoke(this); // 递归调用
        } else {
            // 所有切面执行完毕，执行目标方法
            return method.invoke(target, args);
        }
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public String getSignature() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
}
