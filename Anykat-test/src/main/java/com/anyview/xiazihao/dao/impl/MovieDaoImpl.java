package com.anyview.xiazihao.dao.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.dao.MovieDao;

@KatComponent
public class MovieDaoImpl implements MovieDao {

    @Override
    public String getMovieName() {
        return "ka-cat-test";
    }
}
