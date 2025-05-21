package com.anyview.xiazihao.service.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.MovieDao;
import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.service.MovieService;

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
            return movieDao.selectMovieById(id);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMovie(Movie movie) {
        try {
            movieDao.updateMovie(movie);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addMovie(Movie movie) {
        try {
            movieDao.addMovie(movie);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @KatTransactional
    public void deleteMoviesByIds(List<Integer> ids) throws FileNotFoundException {
        try {
            if(ids == null || ids.isEmpty()) {
                return;
            }
            movieDao.deleteMoviesByIds(ids);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult<Movie> selectMoviesByPage(MovieQueryParam param) throws SQLException, FileNotFoundException {
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        Integer total = movieDao.countMovies(param);
        List<Movie> movies = movieDao.selectMoviesByPage(param);
        return new PageResult<>(total, movies);
    }

    @Override
    public Double selectMovieLowestPriceByScreeningId(Integer movieId) throws SQLException, FileNotFoundException {
        return movieDao.selectMovieLowestPriceByScreeningId(movieId);
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
