package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.ContainerFactory.Annotation.KatAutowired;
import com.anyview.xiazihao.ContainerFactory.Annotation.KatComponent;
import com.anyview.xiazihao.dao.MovieDao;
import com.anyview.xiazihao.entity.test.Movie;
import com.anyview.xiazihao.service.MovieService;

import java.util.ArrayList;
import java.util.List;

@KatComponent
public class MovieServiceImpl implements MovieService {

    @KatAutowired
    private MovieDao movieDao;

    @Override
    public List<Movie> getMovies() {
        List<Movie> list = new ArrayList<>();
        Movie movie = new Movie();
        movie.setTitle(movieDao.getMovieName());
        list.add(movie);
        return list;
    }
}
