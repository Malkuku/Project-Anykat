package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface MovieDao {
    String getMovieName();

    //统计电影数量
    Integer countMovies(MovieQueryParam param, Connection conn,boolean isAutoCloseConn) throws SQLException;

    //分页条件查询
    List<Movie> selectMoviesByPage(MovieQueryParam param, Connection conn, boolean isAutoCloseConn) throws SQLException;

    //批量删除电影
    void deleteMoviesByIds(List<Integer> ids,Connection conn,boolean isAutoCloseConn) throws SQLException;

    //添加电影
    void addMovie(Movie movie,Connection conn,boolean isAutoCloseConn) throws SQLException;

    //根据ID修改电影
    void updateMovie(Movie movie,Connection conn,boolean isAutoCloseConn) throws SQLException;

    //根据ID查询电影
    Movie selectMovieById(Integer id,Connection conn,boolean isAutoCloseConn) throws SQLException;

    //查询电影最低价格
    Double selectMovieLowestPriceByScreeningId(Integer movieId, Connection connection, boolean isAutoCloseConn) throws SQLException;
}
