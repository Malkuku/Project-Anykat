package com.anyview.xiazihao.service;

import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.entity.result.PageResult;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface MovieService {
    List<Movie> getMovies();

    //分页条件查询电影
    PageResult<Movie> selectMoviesByPage(MovieQueryParam param) throws SQLException, FileNotFoundException;

    //批量删除电影
    void deleteMoviesByIds(List<Integer> ids) throws FileNotFoundException;

    //添加电影
    void addMovie(Movie movie);

    //根据ID修改电影
    void updateMovie(Movie movie);

    //根据ID查询电影
    Movie selectMovieById(Integer id);

    //查询电影最低票价
    Double selectMovieLowestPriceByScreeningId(Integer movieId) throws SQLException, FileNotFoundException;
}
