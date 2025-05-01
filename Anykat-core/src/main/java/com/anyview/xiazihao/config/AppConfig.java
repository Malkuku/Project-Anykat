package com.anyview.xiazihao.config;

import com.anyview.xiazihao.entity.config.ContainerConfig;
import com.anyview.xiazihao.entity.config.JwtConfig;
import com.anyview.xiazihao.entity.config.TomcatConfig;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Data
public class AppConfig {
    private TomcatConfig tomcat;
    private JwtConfig jwt;
    private ContainerConfig container;

    // 私有静态实例变量
    private static volatile AppConfig instance;

    // 私有构造函数防止外部实例化
    private AppConfig() throws FileNotFoundException {}

    // 获取单例的静态方法
    public static AppConfig getInstance() throws FileNotFoundException {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                    //  加载配置文件
                    Yaml yaml = new Yaml();
                    instance = yaml.loadAs(
                            new FileInputStream("config.yml"),
                            AppConfig.class
                    );
                }
            }
        }
        return instance;
    }
}