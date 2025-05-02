package com.anyview.xiazihao.dao.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.MovieDao;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class MovieDaoImpl implements MovieDao {

    @Override
    public Movie selectMovieById(Integer id, Connection conn, boolean isAutoCloseConn) throws SQLException {
        String sql = "SELECT * FROM movies WHERE id = ?";
        List<Movie> movieList = JdbcUtils.executeQuery(
                conn,
                sql,
                isAutoCloseConn,
                rs -> {
                    Movie movie = new Movie();
                    try {
                        movie.setId(rs.getInt("id"));
                        movie.setTitle(rs.getString("title"));
                        movie.setReleaseDate(rs.getDate("release_date").toLocalDate());
                        movie.setPosterUrl(rs.getString("poster_url"));
                        movie.setDuration(rs.getInt("duration"));
                        movie.setGenre(rs.getString("genre"));
                        movie.setRating(rs.getDouble("rating"));
                        movie.setStatus(rs.getInt("status"));
                        movie.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        movie.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return movie;
                },
                id
        );
        return movieList.isEmpty() ? null : movieList.get(0);
    }
    @Override
    public String getMovieName() {
        return "ka-cat-test";
    }
}
