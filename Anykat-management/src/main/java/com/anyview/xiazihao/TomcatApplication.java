package com.anyview.xiazihao;

import com.anyview.xiazihao.classPathScanner.ClassPathScanner;
import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.containerFactory.ContainerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class TomcatApplication {
    public static void main(String[] args) throws Exception {
        // 加载配置信息
        final int PORT = AppConfig.getInstance().getTomcat().getPort();
        final String WEBAPP_DIR = AppConfig.getInstance().getTomcat().getWebappDir();
        final String CLASSES_DIR = AppConfig.getInstance().getTomcat().getClassesDir();
        final String JAR_CLASS_DIR = AppConfig.getInstance().getTomcat().getJarClassesDir();
        log.debug("PORT:{}", PORT);
        log.debug("WEBAPP_DIR:{}", WEBAPP_DIR);
        log.debug("CLASSES_DIR:{}", CLASSES_DIR);
        log.debug("JAR_CLASS_DIR:{}", JAR_CLASS_DIR);

        // 1. 创建Tomcat实例
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);

        // 2. 获取webapp目录路径
        String webappPath = getWebappPath(WEBAPP_DIR);
        Context ctx = tomcat.addWebapp("", webappPath);

        // 3. 配置资源
        StandardRoot resources = new StandardRoot(ctx);

        // 判断是否在JAR中运行
        if (isRunningInJar()) {
            // JAR模式下的资源加载
            String jarPath = getJarPath();
            resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/classes", jarPath, JAR_CLASS_DIR));
        } else {
            // 普通文件系统模式
            File classesDir = new File(CLASSES_DIR);
            if (classesDir.exists()) {
                resources.addPreResources(new DirResourceSet(resources,
                        "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
            }
        }

        ctx.setResources(resources);
        ctx.setReloadable(true);

        // 4. 启动服务器
        String serverIp = InetAddress.getLocalHost().getHostAddress(); // 获取本机 IP
        tomcat.getConnector().setAttribute("address", serverIp); // 绑定到服务器 IP
        tomcat.start();

        // 5. 设置类加载器
        ClassLoader tomcatLoader = ctx.getLoader().getClassLoader();
        Thread.currentThread().setContextClassLoader(tomcatLoader);
        ClassPathScanner.setClassLoader(tomcatLoader);

        // 6. 初始化容器工厂
        final ContainerFactory containerFactory = new ContainerFactory();
        ctx.getServletContext().setAttribute("ContainerFactory", containerFactory);

        log.info("Server running at http://{}:{}", serverIp, tomcat.getConnector().getPort());
        tomcat.getServer().await();
    }

    private static String getWebappPath(String webappDir) throws URISyntaxException, IOException {
        // 1. 首先尝试从类路径加载（适用于JAR模式）
        URL resourceUrl = TomcatApplication.class.getClassLoader().getResource(webappDir);
        if (resourceUrl != null) {
            if (resourceUrl.getProtocol().equals("file")) {
                // 开发环境文件系统路径
                return Paths.get(resourceUrl.toURI()).toString();
            } else if (resourceUrl.getProtocol().equals("jar")) {
                // JAR包内资源
                return extractWebappFromJar(resourceUrl);
            }
        }

        // 2. 尝试作为文件系统路径直接访问（开发环境备用方案）
        File file = new File(webappDir);
        if (file.exists()) {
            return file.getAbsolutePath();
        }

        // 3. 尝试从项目标准位置查找（兼容旧配置）
        File altPath = new File("src/main/" + webappDir);
        if (altPath.exists()) {
            return altPath.getAbsolutePath();
        }

        throw new RuntimeException("Webapp directory not found in: \n" +
                "- Classpath: " + webappDir + "\n" +
                "- Filesystem: " + file.getAbsolutePath() + "\n" +
                "- Alternative: " + altPath.getAbsolutePath());
    }

    private static boolean isRunningInJar() {
        String protocol = Objects.requireNonNull(TomcatApplication.class.getResource("")).getProtocol();
        return "jar".equals(protocol);
    }

    private static String getJarPath() {
        URL url = TomcatApplication.class.getProtectionDomain().getCodeSource().getLocation();
        return url.getPath();
    }

    private static String extractWebappFromJar(URL jarUrl) throws IOException {
        File tempDir = Files.createTempDirectory("anykat-webapp").toFile();
        tempDir.deleteOnExit();

        String jarPath = jarUrl.getPath().replaceFirst("file:", "").split("!")[0];
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);  // 处理路径编码

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("webapp/") && !entry.isDirectory()) {
                    String relativePath = entry.getName().substring("webapp/".length());
                    File destFile = new File(tempDir, relativePath);
                    Files.createDirectories(destFile.getParentFile().toPath());
                    try (InputStream is = jarFile.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(destFile.toPath())) {
                        is.transferTo(os);
                    }
                }
            }
        }
        return new File(tempDir, "webapp").getAbsolutePath();
    }
}