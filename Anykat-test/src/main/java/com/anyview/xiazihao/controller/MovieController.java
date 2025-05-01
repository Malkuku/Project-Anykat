package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.ContainerFactory;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.entity.pojo.Movie;
import com.anyview.xiazihao.entity.result.Result;
import com.anyview.xiazihao.service.MovieService;
import com.anyview.xiazihao.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

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

    //get请求入口
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        // 处理/movies/{id}请求
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            try {
                selectMovieById(req, resp);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //根据id查询电影
    private void selectMovieById(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String pathInfo = req.getPathInfo();
        String idStr = pathInfo.substring(1); // 去掉开头的"/"
        Integer id = Integer.parseInt(idStr);
        log.info("接收到的电影ID:{}", id);
        Movie movie = movieService.selectMovieById(id);
        log.info("查询到的电影信息:{}", movie);
        ServletUtils.sendResponse(resp, Result.success(movie));
    }
}
