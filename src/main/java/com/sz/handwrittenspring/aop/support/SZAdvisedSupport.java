package com.sz.handwrittenspring.aop.support;

import com.sz.handwrittenspring.aop.aspect.SZAdivce;
import com.sz.handwrittenspring.aop.config.SZAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/19 13:08
 */
public class SZAdvisedSupport {

    private Class targetClass;
    private Object target;
    private SZAopConfig config;
    private Pattern pointCutClassPattern;
    Map<Method, Map<String, SZAdivce>> methodCache;

    public SZAdvisedSupport(SZAopConfig config) {
        this.config = config;
    }

    public boolean ponitCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.getName()).matches();
    }

    public Map<String, SZAdivce> getAdvices(Method method, Class targetClass) throws Exception {
        Map<String, SZAdivce> cache = methodCache.get(method);

        if (cache == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(method, cache);
        }
        return cache;
    }

    private void parse() {
        //public .* com.sz.handwirttenspring.service..*Service..*(.*)
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);

        pointCutClassPattern = Pattern.compile(pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {
            methodCache = new HashMap<>();

            //先缓存所有的通知回调方法
            Map<String, Method> aspectMethods = new HashMap<>();
            Class aspectClass = Class.forName(this.config.getAspectClass());
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            Pattern pointCutPattern = Pattern.compile(pointCut);

            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();

                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(methodString);
                if (matcher.matches()) {
                    Map<String, SZAdivce> advices = new HashMap<>();

                    //前置通知
                    if (!(config.getAspectBefore() == null || "".equals(config.getAspectBefore()))) {
                        advices.put("before", new SZAdivce(aspectClass.newInstance(), aspectMethods.get(config.getAspectBefore())));
                    }

                    //后置通知
                    if (!(config.getAspectAfter() == null || "".equals(config.getAspectAfter()))) {
                        advices.put("after", new SZAdivce(aspectClass.newInstance(), aspectMethods.get(config.getAspectAfter())));
                    }

                    //异常通知
                    if (!(config.getAspectAfterThrow() == null || "".equals(config.getAspectAfterThrow()))) {
                        SZAdivce adivce = new SZAdivce(aspectClass.newInstance(), aspectMethods.get(config.getAspectAfterThrow()));
                        adivce.setThrowName(config.getAspectAfterThrowingName());
                        advices.put("afterThrowing", adivce);
                    }

                    methodCache.put(method, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public SZAopConfig getConfig() {
        return config;
    }

    public void setConfig(SZAopConfig config) {
        this.config = config;
    }

}
