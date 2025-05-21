package com.anyview.xiazihao.aspectProcessor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KatAround {
    String value(); // 切点表达式，例如："com.example.service.*.*(..)"
}
