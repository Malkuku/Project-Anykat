package com.anyview.xiazihao.aspect;

import com.anyview.xiazihao.aspectProcessor.ProceedingJoinPoint;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAround;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAspect;
import com.anyview.xiazihao.aspectProcessor.annotation.KatOrder;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KatAspect
public class TestAspect {
    @KatAround("com.anyview.xiazihao.service.impl.*.*(..)")
    @KatOrder(1)
    public Object testAround(ProceedingJoinPoint jp) throws Exception {
        log.info("before {}",jp.method().getName());
        Object result = jp.proceed();
        log.info("after {}",jp.method().getName());
        return result;
    }

    @KatAround("com.anyview.xiazihao..*.*(..)")
    @KatOrder(2)
    public Object testAround2(ProceedingJoinPoint jp) throws Exception {
        log.info("before2 {}",jp.method().getName());
        Object result = jp.proceed();
        log.info("after2 {}",jp.method().getName());
        return result;
    }
}
