package com.sz.handwrittenspring.aop;

import com.sz.handwrittenspring.aop.aspect.SZAdivce;
import com.sz.handwrittenspring.aop.support.SZAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/19 13:09
 */
public class SZJdkDynamicAopProxy implements InvocationHandler {

    private SZAdvisedSupport config;

    public SZJdkDynamicAopProxy(SZAdvisedSupport config) {
        this.config = config;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.config.getTargetClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Map<String, SZAdivce> adivces = this.config.getAdvices(method, this.config.getTargetClass());

        Object returnValue = null;
        try {
            invokeAdvice(adivces.get("before"));

            returnValue = method.invoke(this.config.getTarget(), args);

            invokeAdvice(adivces.get("after"));
        } catch (Exception e) {
            invokeAdvice(adivces.get("afterThrowing"));
        }

        return returnValue;
    }

    private void invokeAdvice(SZAdivce adivce) {
        try {
            adivce.getAdviceMethod().invoke(adivce.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
