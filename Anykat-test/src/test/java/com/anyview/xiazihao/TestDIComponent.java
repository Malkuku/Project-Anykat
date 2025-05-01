package com.anyview.xiazihao;

import com.anyview.xiazihao.ContainerFactory.Annotation.KatAutowired;
import com.anyview.xiazihao.ContainerFactory.ContainerFactory;
import com.anyview.xiazihao.service.MovieService;
import org.junit.Before;
import org.junit.Test;

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
