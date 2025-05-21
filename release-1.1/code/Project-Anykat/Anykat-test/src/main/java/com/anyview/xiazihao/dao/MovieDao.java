package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface MovieDao {
    String getMovieName();

    //统计电影数量
    Integer countMovies(MovieQueryParam param) throws SQLException, FileNotFoundException;

    //分页条件查询
    List<Movie> selectMoviesByPage(MovieQueryParam param) throws SQLException, FileNotFoundException;

    //批量删除电影
    void deleteMoviesByIds(List<Integer> ids) throws SQLException, FileNotFoundException;

    //添加电影
    void addMovie(Movie movie) throws SQLException, FileNotFoundException;

    //根据ID修改电影
    void updateMovie(Movie movie) throws SQLException, FileNotFoundException;

    //根据ID查询电影
    Movie selectMovieById(Integer id) throws SQLException, FileNotFoundException;

    //查询电影最低价格
    Double selectMovieLowestPriceByScreeningId(Integer movieId) throws SQLException, FileNotFoundException;
}
