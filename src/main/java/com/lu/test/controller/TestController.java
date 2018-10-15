package com.lu.test.controller;

import com.lu.mvc.annotation.MyAutowired;
import com.lu.mvc.annotation.MyController;
import com.lu.mvc.annotation.MyRequestMapping;
import com.lu.test.service.TestService;

@MyController("TestController1111111")
@MyRequestMapping("test")
public class TestController {

    @MyAutowired
    private TestService testService;

    @MyRequestMapping("/index")
    public String testIoc() {
        System.out.println("进入方法");
        System.out.println(testService.test());
        return null;
    }
}
