package com.sz.handwrittenspring.annotation;

import java.lang.annotation.*;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/2 23:58
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SZRequestMapping {
    String value() default "";
}
