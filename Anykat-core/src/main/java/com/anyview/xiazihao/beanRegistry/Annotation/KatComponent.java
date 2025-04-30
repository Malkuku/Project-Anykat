package com.anyview.xiazihao.beanRegistry.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 组件注解，标记需要由容器管理的类
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KatComponent {
    String value() default "";
}