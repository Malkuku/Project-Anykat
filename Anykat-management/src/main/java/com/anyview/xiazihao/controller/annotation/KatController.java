package com.anyview.xiazihao.controller.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KatController {
    String value() default "";
}