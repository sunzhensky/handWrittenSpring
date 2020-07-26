package com.sz.handwrittenspring.aop.aspect;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/16 15:44
 */
public class TestAspect {

    public void before() {
        System.out.print("Invoker Before Method!!\n");
    }

    public void after() {
        System.out.print("Invoker After Method!!\n");
    }

    public void afterThrowing() {
        System.out.print("出现异常!!\n");
    }

}
