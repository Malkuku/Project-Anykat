package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.beanRegistry.Annotation.KatAutowired;
import com.anyview.xiazihao.beanRegistry.Annotation.KatComponent;
import com.anyview.xiazihao.service.MovieService;

@KatComponent
public class MovieController {

    @KatAutowired
    private MovieService movieService;

    public void getMovies() {
        movieService.getMovies();
    }
}
