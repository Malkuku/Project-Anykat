package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.beanRegistry.Annotation.KatComponent;
import com.anyview.xiazihao.entity.test.Movie;
import com.anyview.xiazihao.service.MovieService;

import java.util.ArrayList;
import java.util.List;

@KatComponent
public class MovieServiceImpl implements MovieService {
    @Override
    public List<Movie> getMovies() {
        return new ArrayList<>();
    }
}
