package com.anyview.xiazihao;

import com.anyview.xiazihao.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

@Slf4j
public class TomcatApplication {
    public static void main(String[] args) throws Exception {
        //加载配置信息
        final int PORT = AppConfig.getInstance().getTomcat().getPort();
        final String WEBAPP_DIR = AppConfig.getInstance().getTomcat().getWebappDir();
        final String CLASSES_DIR = AppConfig.getInstance().getTomcat().getClassesDir();
        log.info("PORT:{}",PORT);
        log.info("WEBAPP_DIR:{}",WEBAPP_DIR);
        log.info("CLASSES_DIR:{}",CLASSES_DIR);

        // 1. 创建Tomcat实例
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);

        // 2. 配置webapp目录并获取Context
        String webappDir = new File(WEBAPP_DIR).getAbsolutePath();
        Context ctx = tomcat.addWebapp("", webappDir);

        // 确保 target/classes 目录被添加到类加载路径中
        File classesDir = new File(CLASSES_DIR);
        ctx.setReloadable(true); // 允许热加载
        ctx.setResources(new StandardRoot(ctx) {{
            addPreResources(new DirResourceSet(this,
                    "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
        }});

        // 3. 启动服务器
        tomcat.start();
        log.info("Server running at http://localhost:{}",tomcat.getConnector().getPort());
        tomcat.getServer().await();
    }
}
