package com.sz.handwrittenspring.aop;

import com.sz.handwrittenspring.aop.context.SZApplicationContext;
import com.sz.handwrittenspring.service.IDemoService;
import com.sz.handwrittenspring.service.impl.DemoServiceImpl;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/21 9:50
 */
public class ApplicationTest {
    public static void main(String[] args) {
        SZApplicationContext app = new SZApplicationContext("application.properties");
        IDemoService iDemoService = (IDemoService) app.getBean(DemoServiceImpl.class);
        iDemoService.get("ss");
    }
}
