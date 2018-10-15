package com.lu.test.controller;

import com.lu.mvc.annotation.MyAutowired;
import com.lu.mvc.annotation.MyController;
import com.lu.mvc.annotation.MyRequestMapping;
import com.lu.test.service.TestService;

import java.util.HashMap;
import java.util.Map;

@MyController
@MyRequestMapping("test")
public class TestController {


    @MyAutowired
    private TestService testService;

    @MyRequestMapping("/index")
    public Map<String, Object> testIoc() {
        System.out.println("进入方法");
        System.out.println(testService.test());
        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        return map;
    }

    @MyRequestMapping("/index1")
    public String testIoc1() {
        System.out.println("进入方法");
        System.out.println(testService.test());
        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        return "index";
    }
}
