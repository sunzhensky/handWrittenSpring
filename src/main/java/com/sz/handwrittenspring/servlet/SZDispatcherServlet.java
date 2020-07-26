package com.sz.handwrittenspring.servlet;

import com.sz.handwrittenspring.annotation.SZAutowired;
import com.sz.handwrittenspring.annotation.SZController;
import com.sz.handwrittenspring.annotation.SZRequestMapping;
import com.sz.handwrittenspring.annotation.SZService;
import com.sz.handwrittenspring.aop.SZJdkDynamicAopProxy;
import com.sz.handwrittenspring.aop.config.SZAopConfig;
import com.sz.handwrittenspring.aop.support.SZAdvisedSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @description: 手写spring
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/2 0:03
 */
public class SZDispatcherServlet extends HttpServlet {

    //ioc容器，存放实例化的对象
    private Map<String, Object> ioc = new HashMap<String, Object>();

    //获取配置文件内容
    private Properties contextConfig = new Properties();

    //存放扫描到的文件路径
    private List<String> classNames = new ArrayList<>();

    //存放http请求与方法的对应关系
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.完成url的调度
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception Detail :" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        Map<String, String[]> parameterMap = req.getParameterMap();
        //硬编码，感受下思想就行(对应SZDispatcherServlet类中的query方法)
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName), new Object[]{req, resp, parameterMap.get("name")[0]});

    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3.实例化相关的类，并且缓存到ico容器
        doInstance();

        //4.完成依赖注入
        doAutowired();

        //5.初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("SZ spring framework Initialization is complete");
    }

    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) return;

        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();
                if (!clazz.isAnnotationPresent(SZController.class)) continue;

                String baseUrl = "";
                if (clazz.isAnnotationPresent(SZRequestMapping.class)) {
                    SZRequestMapping requestMapping = clazz.getAnnotation(SZRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                for (Method method : clazz.getMethods()) {
                    if (!method.isAnnotationPresent(SZRequestMapping.class)) continue;

                    SZRequestMapping requestMapping = method.getAnnotation(SZRequestMapping.class);
                    String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");


                    if (handlerMapping.containsKey(url)) {
                        throw new Exception("The handlerMapping is exist");
                    }
                    handlerMapping.put(url, method);
                    System.out.println("Mapped" + url + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(SZAutowired.class)) continue;

                SZAutowired autowired = field.getAnnotation(SZAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                //暴力强制访问
                field.setAccessible(true);
                try {
                    //传说中的依赖注入
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    private void doInstance() {
        if (classNames.isEmpty()) return;

        try {
            for (String className : classNames) {
                Class clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(SZController.class)) {
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(SZService.class)) {

                    //1.默认是类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());

                    //2.自定义命名
                    SZService service = (SZService) clazz.getAnnotation(SZService.class);
                    if (!"".equals(service.value())) {
                        beanName = service.value();
                    }

                    Object instance = clazz.newInstance();

                    //-------------AOP相关（将代理类注册到ioc容器中）-------------
                    SZAdvisedSupport config = instantionAopConfig();
                    config.setTargetClass(clazz);
                    config.setTarget(instance);

                    if (config.ponitCutMatch()) {
                        instance = new SZJdkDynamicAopProxy(config).getProxy();
                    }
                    //-----------------------------------------------------------
                    ioc.put(beanName, instance);

                    //3.如果是接口
                    for (Class i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The beanNames is exist");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    /**
     * 类首字母小写
     *
     * @param simpleName 类名
     * @return 结果
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) continue;
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {

        //获取配置文件的文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
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

}
