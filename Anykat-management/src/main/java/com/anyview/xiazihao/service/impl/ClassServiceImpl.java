package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.ClassDao;
import com.anyview.xiazihao.service.ClassService;

@KatComponent
@KatSingleton
public class ClassServiceImpl implements ClassService {
    @KatAutowired
    private ClassDao classDao;
}
