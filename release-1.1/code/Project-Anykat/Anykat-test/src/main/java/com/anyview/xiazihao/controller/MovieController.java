package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.ContainerFactory;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.entity.param.MovieQueryParam;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.result.Result;
import com.anyview.xiazihao.service.MovieService;
import com.anyview.xiazihao.utils.JsonUtils;
import com.anyview.xiazihao.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@WebServlet("/movies/*")
public class MovieController extends HttpServlet {

    @KatAutowired
    private MovieService movieService;

    @Override
    public void init() throws ServletException {
        super.init();
        // 从 ServletContext 获取 ContainerFactory
        ContainerFactory factory = (ContainerFactory) getServletContext().getAttribute("ContainerFactory");
        if (factory == null) {
            throw new ServletException("ContainerFactory 未初始化！");
        }
        try {
            factory.injectDependencies(this); // 手动注入依赖
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //delete请求入口
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        deleteMoviesByIds(req, resp);
    }

    //put请求入口
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        updateMovie(req, resp);
    }

    //get请求入口
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        // 处理/movies/{id}请求
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            selectMovieById(req, resp);
        }
        // 处理/movies/price/{movieId}请求
        else if (pathInfo != null && pathInfo.matches("/price/\\d+")) {
            try {
                selectMovieLowestPriceByScreeningId(req, resp);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        // 处理/movies?query=params请求
        else {
            try {
                selectMoviesByPage(req, resp);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //post请求入口
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addMovie(req, resp);
    }

    //根据id查询电影
    private void selectMovieById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String idStr = pathInfo.substring(1); // 去掉开头的"/"
        Integer id = Integer.parseInt(idStr);
        log.info("接收到的电影ID:{}", id);
        Movie movie = movieService.selectMovieById(id);
        log.info("查询到的电影信息:{}", movie);
        ServletUtils.sendResponse(resp, Result.success(movie));
    }

    //根据id修改电影
    private void updateMovie(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = ServletUtils.getRequestBody(req);
        Movie movie = JsonUtils.parseJson(json, Movie.class);
        log.info("接收到的电影信息:{}", movie);
        movieService.updateMovie(movie);
        ServletUtils.sendResponse(resp, Result.success());
    }

    //添加电影
    private void addMovie(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = ServletUtils.getRequestBody(req);
        Movie movie = JsonUtils.parseJson(json, Movie.class);
        log.info("接收到的电影信息:{}", movie);
        movieService.addMovie(movie);
        ServletUtils.sendResponse(resp, Result.success());
    }

    //条件分页查询电影信息
    private void selectMoviesByPage(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        MovieQueryParam param = new MovieQueryParam();
        {
            param.setTitle(req.getParameter("title"));
            param.setReleaseDate(
                    req.getParameter("releaseDate") != null ? LocalDate.parse(req.getParameter("releaseDate")) : null
            );
            param.setGenre(req.getParameter("genre"));
            param.setMinDuration(
                    req.getParameter("minDuration") != null ? Integer.parseInt(req.getParameter("minDuration")) : null
            );
            param.setMaxDuration(
                    req.getParameter("maxDuration") != null ? Integer.parseInt(req.getParameter("maxDuration")) : null
            );
            param.setMinRating(
                    req.getParameter("minRating") != null ? Double.valueOf(req.getParameter("minRating")) : null
            );
            param.setMaxRating(
                    req.getParameter("maxRating") != null ? Double.valueOf(req.getParameter("maxRating")) : null
            );
            param.setStatus(
                    req.getParameter("status") != null ? Integer.parseInt(req.getParameter("status")) : null
            );
            param.setPage(
                    req.getParameter("page") != null ? Integer.parseInt(req.getParameter("page")) : param.getPage()
            );
            param.setPageSize(
                    req.getParameter("pageSize") != null ? Integer.parseInt(req.getParameter("pageSize")) : param.getPageSize()
            );
        }
        log.info("接收到的电影查询参数:{}", param);
        PageResult<Movie> pageResult;
        try {
            pageResult = movieService.selectMoviesByPage(param);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info("查询到的电影列表:{}", pageResult);
        ServletUtils.sendResponse(resp, Result.success(pageResult));
    }

    //批量删除电影
    private void deleteMoviesByIds(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] id_strs = req.getParameterValues("ids");
        log.info("接收到的电影ID:{}", (Object) id_strs);
        List<Integer> ids = new ArrayList<>();
        {
            if (id_strs != null) {
                String[] idArray = id_strs[0].split(",");
                for (String id_str : idArray) {
                    log.info("处理ID:{}", id_str);
                    try {
                        ids.add(Integer.parseInt(id_str));
                    } catch (NumberFormatException e) {
                        log.error("ID格式错误:{}", id_str, e);
                    }
                }
            }
        }
        movieService.deleteMoviesByIds(ids);
        ServletUtils.sendResponse(resp, Result.success());
    }

    //根据电影Id查询场次最低价格
    private void selectMovieLowestPriceByScreeningId(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String pathInfo = req.getPathInfo();
        int movieId = Integer.parseInt(pathInfo.replaceAll("\\D", ""));
        Double lowestPrice = movieService.selectMovieLowestPriceByScreeningId(movieId);
        log.info("查询成功,结果:{}", lowestPrice);
        ServletUtils.sendResponse(resp, Result.success(lowestPrice));
    }
}
