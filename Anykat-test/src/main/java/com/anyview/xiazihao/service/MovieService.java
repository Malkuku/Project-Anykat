package com.anyview.xiazihao.service;

import com.anyview.xiazihao.entity.pojo.Movie;

import java.sql.SQLException;
import java.util.List;

public interface MovieService {
    List<Movie> getMovies();

    //根据ID查询电影
    Movie selectMovieById(Integer id) throws SQLException;
}
