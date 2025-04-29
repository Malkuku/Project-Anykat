package com.anyview.xiazihao.entity.config;

import lombok.Data;

@Data
public class JwtConfig {
    //Jwt密钥
    private String secretKey; // 秘钥
    //管理员Jwt密钥
    private String adminSecretKey;
    //设置有效时间
    private long expireTime; // 6小时
}
