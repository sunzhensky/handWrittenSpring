package com.sz.handwrittenspring.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/16 17:55
 */
@Data
public class SZAdivce {

    private Object aspect;
    private Method adviceMethod;
    private String throwName;

    public SZAdivce(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

}
