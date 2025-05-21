package com.anyview.xiazihao.entity.config;

import lombok.Data;

@Data
public class HakimiConfig {
    private String url;
    private String username;
    private String password;
    private int maxSize; // 正常最大连接数
    private int minIdle; // 最小空闲连接数
    private long maxWaitMillis; // 获取连接的最大等待时间
    private int maxWaitThreads; // 触发扩容的等待线程阈值（建议=maxSize*0.3）
    private long idleTimeoutMillis;       // 空闲连接超时时间（默认30分钟）
}
