package com.sz.handwrittenspring.service.impl;

import com.sz.handwrittenspring.service.IDemoService;
import com.sz.handwrittenspring.annotation.SZService;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/4 23:23
 */
@SZService
public class DemoServiceImpl implements IDemoService {

    @Override
    public String get(String name) {
        System.out.println("Invoker DemoService get method!!");
        int a = 1 / 0;
        return "My name is " + name + ",from service";
    }

}
