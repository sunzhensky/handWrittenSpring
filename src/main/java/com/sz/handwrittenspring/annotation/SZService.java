package com.sz.handwrittenspring.annotation;

import java.lang.annotation.*;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/2 23:11
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SZService {
    String value() default "";
}
