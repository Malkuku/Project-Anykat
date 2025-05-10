package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.service.ClassService;

@KatComponent
@KatController
@KatRequestMapping(path = "/classes")
public class ClassController {
    @KatAutowired
    private ClassService classService;
}
