package com.lu.test.controller;

import com.lu.mvc.annotation.MyAutowired;
import com.lu.mvc.annotation.MyController;
import com.lu.test.service.TestService;

@MyController("TestController1111111")
public class TestController {

    @MyAutowired
    TestService testService;
}
