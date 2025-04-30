package com.anyview.xiazihao.beanRegistry.Annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 单例作用域注解
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KatSingleton {
}
