package com.anyview.xiazihao.dao;

import com.anyview.xiazihao.entity.pojo.Movie;

import java.sql.Connection;
import java.sql.SQLException;

public interface MovieDao {
    String getMovieName();

    //根据ID查询电影
    Movie selectMovieById(Integer id, Connection conn, boolean isAutoCloseConn) throws SQLException;
}
