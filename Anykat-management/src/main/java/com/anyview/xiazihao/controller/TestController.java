package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.*;
import com.anyview.xiazihao.entity.test.Movie;

import java.util.List;

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

    @KatRequestMapping(path = "/test3",method = "GET")
    public String test3(
            @KatRequestParam("id") Integer id) {
        return "test3: " + id;
    }

    @KatRequestMapping(path = "/test4",method = "GET")
    public String test4(
            @KatRequestBody() Movie movie){
        return "test4: " + movie;
    }

    @KatRequestMapping(path = "/test5/{userId}",method = "GET")
    public String test5(
            @KatRequestParam("id") Integer id,
            @KatRequestBody() Movie movie){
        return "test5: " + movie + " " + id;
    }

    @KatRequestMapping(path = "/test6",method = "GET")
    public String test6(
            @KatRequestParam("ids") String ids){
        return "test6: " + ids;
    }
}
