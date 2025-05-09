package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatPathVariable;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;

@KatComponent
@KatController
@KatRequestMapping(path = "/api/test")
public class TestController {
    @KatRequestMapping(path = "", method = "GET")
    public String test() {
        return "test";
    }

    @KatRequestMapping(path = "/{userId}", method = "GET")
    public String test2(@KatPathVariable("userId") long userId) {
        return "test2: " + userId;
    }
}
