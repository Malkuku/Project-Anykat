package com.anyview.xiazihao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KatTransactional {
    /**
     * 指定事务超时时间(秒)，默认-1表示使用系统默认值
     */
    int timeout() default -1;

    /**
     * 指定事务隔离级别，默认-1表示使用系统默认值
     */
    int isolation() default -1;

    /**
     * 指定事务是否只读，默认false
     */
    boolean readOnly() default false;
}
