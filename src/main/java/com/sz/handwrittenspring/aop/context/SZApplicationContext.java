package com.sz.handwrittenspring.aop.context;

import com.sz.handwrittenspring.aop.SZJdkDynamicAopProxy;
import com.sz.handwrittenspring.aop.config.SZAopConfig;
import com.sz.handwrittenspring.aop.support.SZAdvisedSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/21 9:50
 */
public class SZApplicationContext {


    //存放实例化的对象
    private Map<String, Object> ioc = new HashMap<String, Object>();

    //获取配置文件内容
    private Properties contextConfig = new Properties();

    public SZApplicationContext(String... configLocations) {
        doLoadConfig(configLocations[0]);
    }

    private void doLoadConfig(String configLocation) {
        //获取配置文件的文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //延时加载机制
    public Object getBean(Class beanClass) {
        String beanName = beanClass.getName();
        Object instance = instaniateBean(beanName);
        ioc.put(beanName, instance);
        return this.ioc.get(beanName);
    }

    private SZAdvisedSupport instantionAopConfig() {
        SZAopConfig config = new SZAopConfig();
        config.setPointCut(contextConfig.getProperty("pointCut"));
        config.setAspectClass(contextConfig.getProperty("aspectClass"));
        config.setAspectBefore(contextConfig.getProperty("aspectBefore"));
        config.setAspectAfter(contextConfig.getProperty("aspectAfter"));
        config.setAspectAfterThrow(contextConfig.getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(contextConfig.getProperty("aspectAfterThrowingName"));

        return new SZAdvisedSupport(config);
    }

    private Object instaniateBean(String beanName) {
        Object instance = null;

        try {

            Class<?> clazz = Class.forName(beanName);
            instance = clazz.newInstance();

            //--------------------------------------------
            //AOP的判断
            SZAdvisedSupport config = instantionAopConfig();
            config.setTargetClass(clazz);
            config.setTarget(instance);

            if (config.ponitCutMatch()) {
                instance = new SZJdkDynamicAopProxy(config).getProxy();
            }
            // --------------------------------------------

        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }
}
