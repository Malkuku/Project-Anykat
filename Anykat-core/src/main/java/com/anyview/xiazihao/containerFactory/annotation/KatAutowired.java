package com.anyview.xiazihao.containerFactory.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 自动装配注解，标记需要注入的依赖
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KatAutowired {
}