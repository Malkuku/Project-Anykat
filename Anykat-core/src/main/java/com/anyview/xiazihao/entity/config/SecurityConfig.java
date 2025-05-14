package com.anyview.xiazihao.entity.config;

import lombok.Data;

@Data
public class SecurityConfig {
    public boolean filterOpen = true; //是否打开过滤器
    public boolean authOpen = true; //是否打开用户身份验证
    public boolean getUserInfoOpen = false; //是否允许获取用户信息
}
