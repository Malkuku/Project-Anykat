package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.dao.MovieDao;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.service.MovieService;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@KatComponent
public class MovieServiceImpl implements MovieService {

    @KatAutowired
    private MovieDao movieDao;

    @Override
    public Movie selectMovieById(Integer id) throws SQLException {
        return movieDao.selectMovieById(id, JdbcUtils.getConnection(), true);
    }

    @Override
    public List<Movie> getMovies() {
        List<Movie> list = new ArrayList<>();
        Movie movie = new Movie();
        movie.setTitle(movieDao.getMovieName());
        list.add(movie);
        return list;
    }
}
