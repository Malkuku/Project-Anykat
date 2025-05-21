package com.anyview.xiazihao.aspect;

import com.anyview.xiazihao.aspectProcessor.ProceedingJoinPoint;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAround;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAspect;
import com.anyview.xiazihao.aspectProcessor.annotation.KatOrder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KatAspect
public class TestAspect {
    @KatAround("com.anyview.xiazihao.service.impl.*.*(..)")
    @KatOrder(1)
    public Object testAround(ProceedingJoinPoint jp) throws Throwable {
        log.info("before {}",jp.getMethod().getName());
        Object result = jp.proceed();
        log.info("after {}",jp.getMethod().getName());
        return result;
    }

    @KatAround("com.anyview.xiazihao..*.*(..)")
    @KatOrder(2)
    public Object testAround2(ProceedingJoinPoint jp) throws Throwable {
        log.info("before2 {}",jp.getMethod().getName());
        Object result = jp.proceed();
        log.info("after2 {}",jp.getMethod().getName());
        return result;
    }
}
