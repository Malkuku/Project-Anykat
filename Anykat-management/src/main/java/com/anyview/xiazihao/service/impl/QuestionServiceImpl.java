package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.QuestionDao;
import com.anyview.xiazihao.service.QuestionService;

@KatComponent
@KatSingleton
public class QuestionServiceImpl implements QuestionService {
    @KatAutowired
    private QuestionDao questionDao;
}
