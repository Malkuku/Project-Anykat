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
import java.util.List;

@KatComponent
@KatSingleton
@Slf4j
public class MovieDaoImpl implements MovieDao {
    @Override
    public Movie selectMovieById(Integer id) throws SQLException, FileNotFoundException {
        String sql = "SELECT * FROM movies WHERE id = ?";
        List<Movie> movieList = JdbcUtils.executeQuery(
                sql, Movie.class,
                id
        );
        return movieList.isEmpty() ? null : movieList.get(0);
    }

    @Override
    public void updateMovie(Movie movie) throws SQLException, FileNotFoundException {
        String sql = "UPDATE movies SET " +
                "title = COALESCE(?, title), " +
                "release_date = COALESCE(?, release_date), " +
                "poster_url = COALESCE(?, poster_url), " +
                "duration = COALESCE(?, duration), " +
                "genre = COALESCE(?, genre), " +
                "rating = COALESCE(?, rating), " +
                "status = COALESCE(?, status), " +
                "updated_at = NOW() " +
                "WHERE id = ?";
        JdbcUtils.executeUpdate( sql,
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getDuration(),
                movie.getGenre(),
                movie.getRating(),
                movie.getStatus(),
                movie.getId()
        );
    }

    @Override
    public void addMovie(Movie movie) throws SQLException, FileNotFoundException {
        String sql = "INSERT INTO movies (title, release_date, poster_url, duration, genre, rating, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        log.info("Executing SQL: {}", sql);
        JdbcUtils.executeUpdate(sql,
                movie.getTitle(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getDuration(),
                movie.getGenre(),
                movie.getRating(),
                movie.getStatus()
        );
    }

    @Override
    public void deleteMoviesByIds(List<Integer> ids) throws SQLException, FileNotFoundException {
        String sql = "DELETE FROM movies WHERE id IN (" +
                String.join(",", ids.stream().map(id -> "?").toArray(String[]::new)) +
                ")";
        log.info("Executing SQL: {}", sql);
        JdbcUtils.executeUpdate(sql,ids.toArray());
    }

    @Override
    public Integer countMovies(MovieQueryParam param) throws SQLException, FileNotFoundException {
        String sql = "SELECT COUNT(*) FROM movies WHERE 1=1 " +
                "AND (? IS NULL OR title LIKE CONCAT('%', ?, '%')) " +
                "AND (? IS NULL OR release_date = ?) " +
                "AND (? IS NULL OR duration >= ?) " +
                "AND (? IS NULL OR duration <= ?) " +
                "AND (? IS NULL OR genre = ?) " +
                "AND (? IS NULL OR rating >= ?) " +
                "AND (? IS NULL OR rating <= ?) " +
                "AND (? IS NULL OR status = ?)";


        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                rs -> {
                    Integer count = 0;
                    try {
                        count = rs.getInt(1);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return count;
                },
                param.getTitle(), param.getTitle(),// title
                param.getReleaseDate(), param.getReleaseDate(), // release_date
                param.getMinDuration(), param.getMinDuration(),  // min_duration
                param.getMaxDuration(), param.getMaxDuration(),  // max_duration
                param.getGenre(), param.getGenre(),              // genre
                param.getMinRating(), param.getMinRating(),     // min_rating
                param.getMaxRating(), param.getMaxRating(),     // max_rating
                param.getStatus(), param.getStatus()            // status
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
                param
        );
    }

    @Override
    public Double selectMovieLowestPriceByScreeningId(Integer movieId) throws SQLException, FileNotFoundException {
        String sql = "select min(price) from screenings where movie_id = ? and status = 1";
        List<Double> priceList = JdbcUtils.executeQuery(
                sql,
                rs -> {
                    Double price = 0.0;
                    try {
                        price = rs.getDouble(1);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return price;
                },
                movieId
        );
        return priceList.isEmpty() ? 0.0 : priceList.get(0);
    }
    @Override
    public String getMovieName() {
        return "ka-cat-test";
    }
}
