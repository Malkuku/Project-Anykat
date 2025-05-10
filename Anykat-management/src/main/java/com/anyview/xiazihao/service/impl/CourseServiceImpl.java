package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.CourseDao;
import com.anyview.xiazihao.service.CourseService;

@KatComponent
@KatSingleton
public class CourseServiceImpl implements CourseService {
    @KatAutowired
    private CourseDao courseDao;
}
