package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@KatComponent
@KatController
@KatRequestMapping(path = "/api/test")
public class TestController {
    @KatRequestMapping(path = "", method = "GET")
    public String test(HttpServletRequest req, HttpServletResponse resp) {
        return "test";
    }
}
