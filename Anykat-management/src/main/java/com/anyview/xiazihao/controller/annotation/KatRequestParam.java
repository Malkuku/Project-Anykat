package com.anyview.xiazihao.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface KatRequestParam {
    String value() default "";  // 参数名
    boolean required() default true;  // 是否必须
    String defaultValue() default "";  // 默认值
}
