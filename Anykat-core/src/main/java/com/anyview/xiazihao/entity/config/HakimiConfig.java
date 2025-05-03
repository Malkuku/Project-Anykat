package com.anyview.xiazihao.entity.config;

import lombok.Data;

@Data
public class HakimiConfig {
    private String url;
    private String username;
    private String password;
    private int maxSize;
    private int minIdle;
    private long maxWaitMillis;
}
