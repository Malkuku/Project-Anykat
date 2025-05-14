package com.anyview.xiazihao.filter;

import com.anyview.xiazihao.config.AppConfig;
import com.anyview.xiazihao.entity.context.AdminContext;
import com.anyview.xiazihao.entity.context.UserContext;
import com.anyview.xiazihao.entity.exception.ValidationFailedException;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.result.Result;
import com.anyview.xiazihao.utils.JwtUtils;
import com.anyview.xiazihao.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            FILTER_OPEN = AppConfig.getInstance().getSecurity().isFilterOpen();
            AUTH_OPEN = AppConfig.getInstance().getSecurity().isAuthOpen();
            GET_USER_INFO_OPEN = AppConfig.getInstance().getSecurity().isGetUserInfoOpen();
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
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if (!FILTER_OPEN) {
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
            Map<String, Object> claims;
            String token = httpRequest.getHeader("token");
            log.info("Token:{}", token);
            //验证Token
            if (token == null || token.isEmpty()) {
                throw new ValidationFailedException("Token不能为空");
            }
            try {
                claims = JwtUtils.parseToken(token);
            } catch (Exception e) {
                throw new ValidationFailedException("Token错误");
            }
            //放行请求
            log.info("Token验证通过");

            //如果开启用户验证，储存用户上下文
            if (AUTH_OPEN) {
                User user = new User();
                user.setId((Integer) claims.get("id"));
                user.setUsername((String) claims.get("username"));
                user.setRole((Integer) claims.get("role"));
                UserContext.setUser(user);
            }

            //如果是管理员，验证管理员Token
            String adminToken = httpRequest.getHeader("adminToken");
            log.info("adminToken:{}", adminToken);
            //验证Token
            if (adminToken == null || adminToken.isEmpty()) {
                throw new ValidationFailedException("adminToken不能为空");
            }
            try {
                claims = JwtUtils.parseAdminToken(adminToken);
            } catch (Exception e) {
                throw new ValidationFailedException("adminToken错误");
            }
            //放行请求
            log.info("adminToken验证通过");

            if (AUTH_OPEN) {
                User user = new User();
                user.setId((Integer) claims.get("id"));
                user.setUsername((String) claims.get("username"));
                user.setRole((Integer) claims.get("role"));
                AdminContext.setUser(user);
            }


            chain.doFilter(request, response);
        }catch (ValidationFailedException e){
            log.error("过滤器捕获异常: {} {}",e, e.getMessage(),e.getCause());
            ServletUtils.sendResponse((HttpServletResponse) response, Result.error(e.getMessage()));
        }
    }


    @Override
    public void destroy() {
        log.debug("过滤器销毁");
    }
}
