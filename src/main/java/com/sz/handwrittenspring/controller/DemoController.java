package com.sz.handwrittenspring.controller;

import com.sz.handwrittenspring.service.IDemoService;
import com.sz.handwrittenspring.annotation.SZAutowired;
import com.sz.handwrittenspring.annotation.SZController;
import com.sz.handwrittenspring.annotation.SZRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description:
 * @version: 1.0
 * @author: zhen.sun@hand-china.com
 * @date: 2020/7/2 23:59
 */
@SZController
@SZRequestMapping("/demo")
public class DemoController {

    @SZAutowired
    private IDemoService iDemoService;


    @SZRequestMapping("/query")
    public void query(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                      String name) {

        String result = iDemoService.get(name);

        try {
            httpServletResponse.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
