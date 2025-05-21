package com.anyview.xiazihao.dao.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.MovieDao;
import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.utils.JdbcUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@KatComponent
@KatSingleton
@Slf4j
public class MovieDaoImpl implements MovieDao {
    @Override
    public Movie selectMovieById(Integer id) throws SQLException, FileNotFoundException {
        String sql = "SELECT * FROM movies WHERE id = #{id}";
        List<Movie> movieList = JdbcUtils.executeQuery(
                sql, Movie.class,
                "id",id
        );
        return movieList.isEmpty() ? null : movieList.get(0);
    }

    @Override
    public void updateMovie(Movie movie) throws SQLException, FileNotFoundException {
        String sql = "UPDATE movies SET " +
                "title = COALESCE(#{title}, title), " +
                "release_date = COALESCE(#{releaseDate}, release_date), " +
                "poster_url = COALESCE(#{posterUrl}, poster_url), " +
                "duration = COALESCE(#{duration}, duration), " +
                "genre = COALESCE(#{genre}, genre), " +
                "rating = COALESCE(#{rating}, rating), " +
                "status = COALESCE(#{status}, status), " +
                "updated_at = NOW() " +
                "WHERE id = #{id}";
        JdbcUtils.executeUpdate(sql,movie);
    }

    @Override
    public void addMovie(Movie movie) throws SQLException, FileNotFoundException {
        String sql = "INSERT INTO movies (title, release_date, poster_url, duration, genre, rating, status) " +
                "VALUES (#{title}, #{releaseDate}, #{posterUrl}, #{duration}, #{genre}, #{rating}, #{status})";
        JdbcUtils.executeUpdate(sql,movie);
    }

    @Override
    public void deleteMoviesByIds(List<Integer> ids) throws SQLException, FileNotFoundException {
        String sql = "DELETE FROM movies WHERE id IN (" +
                String.join(",", ids.stream().map(id -> "?").toArray(String[]::new)) +
                ")";
        JdbcUtils.executeUpdate(sql,ids.toArray());
    }

    @Override
    public Integer countMovies(MovieQueryParam param) throws SQLException, FileNotFoundException {
        String sql = "SELECT COUNT(*) FROM movies WHERE 1=1 " +
                "AND (#{title} IS NULL OR title LIKE CONCAT('%', #{title}, '%')) " +
                "AND (#{releaseDate} IS NULL OR release_date = #{releaseDate}) " +
                "AND (#{minDuration} IS NULL OR duration >= #{minDuration}) " +
                "AND (#{maxDuration} IS NULL OR duration <= #{maxDuration}) " +
                "AND (#{genre} IS NULL OR genre = #{genre}) " +
                "AND (#{minRating} IS NULL OR rating >= #{minRating}) " +
                "AND (#{maxRating} IS NULL OR rating <= #{maxRating}) " +
                "AND (#{status} IS NULL OR status = #{status})";


        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<Movie> selectMoviesByPage(MovieQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
                SELECT *
                FROM movies
                WHERE 1=1\s
                    AND (#{title} IS NULL OR title LIKE CONCAT('%', #{title}, '%'))
                    AND (#{releaseDate} IS NULL OR release_date <= #{releaseDate})
                    AND (#{minDuration} IS NULL OR duration >= #{minDuration})
                    AND (#{maxDuration} IS NULL OR duration <= #{maxDuration})
                    AND (#{genre} IS NULL OR genre LIKE CONCAT('%', #{genre}, '%'))
                    AND (#{minRating} IS NULL OR rating >= #{minRating})
                    AND (#{maxRating} IS NULL OR rating <= #{maxRating})
                    AND (#{status} IS NULL OR status = #{status})
                ORDER BY updated_at desc\s
                LIMIT #{pageSize} OFFSET #{offset}""";

        return JdbcUtils.executeQuery(
                sql,
                Movie.class,
                param,
                "offset", 2
        );
    }

    @Override
    public Double selectMovieLowestPriceByScreeningId(Integer movieId) throws SQLException, FileNotFoundException {
        String sql = "select min(price) from screenings where movie_id = #{movieId} and status = 1";
        List<Double> priceList = JdbcUtils.executeQuery(
                sql,
                Double.class,
                "movieId",movieId
        );
        return priceList.isEmpty() ? 0.0 : priceList.get(0);
    }
    @Override
    public String getMovieName() {
        return "ka-cat-test";
    }
}
