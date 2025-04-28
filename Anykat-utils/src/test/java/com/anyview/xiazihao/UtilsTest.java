package com.anyview.xiazihao;


import com.anyview.xiazihao.test.Movie;
import com.anyview.xiazihao.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
public class UtilsTest {

    //测试Json封装
    @Test
    public void testToJson() {
        Movie movie = new Movie(1, "Example Title", "http://example.com/poster.jpg", LocalDate.parse("2023-10-01"), 120, "Action", 8.5, 1, LocalDateTime.now(), LocalDateTime.now());
        String json = JsonUtils.toJson(movie);
        log.info(json);
    }

    //测试Json解析
    @Test
    public void testParseJson() {
        String json = """
                {
                  "id": 1,
                  "title": "Example Title",
                  "poster_url": "http://example.com/poster.jpg",
                  "release_date": "2023-10-01",
                  "duration": 120,
                  "genre": "Action",
                  "rating": 8.5,
                  "status": 1,
                  "created_at": "2023-10-01T12:00:00",
                  "updated_at": "2023-10-01T12:00:00"
                }""";
        Movie movie = JsonUtils.parseJson(json, Movie.class);
        log.info(movie.toString());
        json = """
                    [
                        {
                            "id": 1,
                            "title": "Example Title 1",
                            "poster_url": "http://example.com/poster1.jpg",
                            "release_date": "2023-10-01",
                            "duration": 120,
                            "genre": "Action",
                            "rating": 8.5,
                            "status": 1,
                            "created_at": "2023-10-01T12:00:00",
                            "updated_at": "2023-10-01T12:00:00"
                        },
                        {
                            "id": 2,
                            "title": "Example Title 2",
                            "poster_url": "http://example.com/poster2.jpg",
                            "release_date": "2023-10-02",
                            "duration": 130,
                            "genre": "Comedy",
                            "rating": 7.5,
                            "status": 1,
                            "created_at": "2023-10-02T12:00:00",
                            "updated_at": "2023-10-02T12:00:00"
                        },
                        {
                            "id": 3,
                            "title": "Example Title 3",
                            "poster_url": "http://example.com/poster3.jpg",
                            "release_date": "2023-10-03",
                            "duration": 140,
                            "genre": "Drama",
                            "rating": 9.0,
                            "status": 1,
                            "created_at": "2023-10-03T12:00:00",
                            "updated_at": "2023-10-03T12:00:00"
                        }
                    ]""";
        List<Movie> movies = JsonUtils.parseJsonToList(json, Movie.class);
        log.info(movies.toString());
    }

}
