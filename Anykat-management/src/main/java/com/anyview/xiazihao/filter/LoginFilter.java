package com.anyview.xiazihao.filter;

import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.entity.context.UserContext;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@Slf4j
@WebFilter(urlPatterns = {"/*"})
public class LoginFilter implements Filter {
    private static final boolean FILTER_OPEN;
    private static final boolean AUTH_OPEN;
    private static final boolean GET_USER_INFO_OPEN;
    static {
        try {
            FILTER_OPEN = AppConfig.getInstance().getSecurity().filterOpen;
            AUTH_OPEN = AppConfig.getInstance().getSecurity().authOpen;
            GET_USER_INFO_OPEN = AppConfig.getInstance().getSecurity().getUserInfoOpen;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public LoginFilter() throws FileNotFoundException {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("过滤器初始化");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if(!FILTER_OPEN){
            chain.doFilter(request, response);
            return;
        }

        // 放行登录接口
        if (httpRequest.getRequestURI().contains("/common-users/login") && "POST".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 放行获取用户信息接口
        if (!GET_USER_INFO_OPEN && httpRequest.getRequestURI().contains("/common-users") && "GET".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        //获取请求头的Token
        Map<String,Object> claims;
        String token = httpRequest.getHeader("token");
        log.info("Token:{}", token);
        //验证Token
        if (token == null || token.isEmpty()) {
            throw new ServletException("Token不能为空");
        }
        try {
            claims = JwtUtils.parseToken(token);
        } catch (Exception e) {
            throw new ServletException("Token错误");
        }
        //放行请求
        log.info("Token验证通过");

        //如果开启用户验证，储存用户上下文
         if(AUTH_OPEN){
             User user = new User();
             user.setId((Integer) claims.get("id"));
             user.setUsername((String) claims.get("username"));
             user.setRole((Integer) claims.get("role"));
             UserContext.setUser(user);
         }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.debug("过滤器销毁");
    }
}
