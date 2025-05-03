package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.MovieDao;
import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.MovieService;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@KatSingleton
@KatComponent
public class MovieServiceImpl implements MovieService {

    @KatAutowired
    private MovieDao movieDao;


    @Override
    public Movie selectMovieById(Integer id) {
        try {
            return movieDao.selectMovieById(id, JdbcUtils.getConnection(),true);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMovie(Movie movie) {
        try {
            movieDao.updateMovie(movie, JdbcUtils.getConnection(),true);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addMovie(Movie movie) {
        try {
            movieDao.addMovie(movie, JdbcUtils.getConnection(),true);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMoviesByIds(List<Integer> ids) throws FileNotFoundException {
        try {
            if(ids == null || ids.isEmpty()) {
                return;
            }
            movieDao.deleteMoviesByIds(ids, JdbcUtils.getConnection(),true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult<Movie> selectMoviesByPage(MovieQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = movieDao.countMovies(param, JdbcUtils.getConnection(),true);
        List<Movie> movies = movieDao.selectMoviesByPage(param, JdbcUtils.getConnection(),true);
        return new PageResult<>(total, movies);
    }

    @Override
    public Double selectMovieLowestPriceByScreeningId(Integer movieId) throws SQLException, FileNotFoundException {
        return movieDao.selectMovieLowestPriceByScreeningId(movieId, JdbcUtils.getConnection(),true);
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
