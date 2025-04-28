package com.anyview.xiazihao.config;

import lombok.Data;

@Data
public class TomcatConfig {
    private int port;
    private String webappDir;
    private String classesDir;
}
