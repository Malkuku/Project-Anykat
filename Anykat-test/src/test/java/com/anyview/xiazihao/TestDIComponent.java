package com.anyview.xiazihao;

import com.anyview.xiazihao.beanRegistry.Annotation.KatAutowired;
import com.anyview.xiazihao.beanRegistry.ContainerFactory;
import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.service.MovieService;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class TestDIComponent {
    private ContainerFactory containerFactory;

    @KatAutowired
    private MovieService movieService;
    @Before
    public void init() throws Exception {
        containerFactory = new ContainerFactory();
        containerFactory.injectDependencies(this); // 手动触发注入
    }


    @Test
    public void test() {
        System.out.println(movieService.getMovies());
    }
}
