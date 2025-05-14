package com.anyview.xiazihao.entity.config;

import lombok.Data;

@Data
public class SecurityConfig {
    private boolean filterOpen; //是否打开过滤器
    private boolean authOpen; //是否打开用户身份验证
    private boolean getUserInfoOpen; //是否允许获取用户信息
}
