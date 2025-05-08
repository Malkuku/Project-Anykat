package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.ContainerFactory;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.entity.result.Result;
import com.anyview.xiazihao.utils.PathUtils;
import com.anyview.xiazihao.utils.ServletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@WebServlet("/*")
public class DispatcherController extends HttpServlet {
    // 路由表: 路径 -> 控制器方法映射
    private final Map<RouteKey, HandlerMethod> routeMappings = new ConcurrentHashMap<>();
    private ContainerFactory containerFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 路由键定义
    private record RouteKey(String path, String httpMethod) {
        private RouteKey(String path, String httpMethod) {
            this.path = PathUtils.normalize(path);
            this.httpMethod = httpMethod.toUpperCase();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RouteKey routeKey = (RouteKey) o;
            return path.equals(routeKey.path) &&
                    httpMethod.equals(routeKey.httpMethod);
        }

        @Override
        public String toString() {
            return httpMethod + " " + path;
        }
    }

    // 处理器方法封装
    private record HandlerMethod(Object controllerInstance, Method method) {}

    @Override
    public void init(){
        // 获取ContainerFactory进而获取BeanRegistry
        this.containerFactory = (ContainerFactory) getServletContext()
                .getAttribute("ContainerFactory");
        // 构建路由表
        buildRouteMappings();
    }


    private void buildRouteMappings() {
        // 获取所有控制器类（标记了@KatComponent和@Controller）
        containerFactory.getRegistry().getClassRegistry().forEach((beanName, clazz) -> {
            if (clazz.isAnnotationPresent(KatController.class)) {
                registerControllerRoutes(clazz, containerFactory.getBean(clazz));
            }
        });
    }

    private void registerControllerRoutes(Class<?> controllerClass, Object controllerInstance) {
        // 类级别的路径
        String basePath = "";
        if (controllerClass.isAnnotationPresent(KatRequestMapping.class)) {
            basePath = controllerClass.getAnnotation(KatRequestMapping.class).path();
        }

        // 方法级别的映射
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(KatRequestMapping.class)) {
                KatRequestMapping mapping = method.getAnnotation(KatRequestMapping.class);
                String fullPath = PathUtils.normalize(basePath + mapping.path());
                String httpMethod = mapping.method().toUpperCase();

                RouteKey key = new RouteKey(fullPath, httpMethod);
                routeMappings.put(key, new HandlerMethod(controllerInstance, method));
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // 标准化请求路径
        String path = PathUtils.normalize(req.getRequestURI().replace(req.getContextPath(), ""));
        String method = req.getMethod().toUpperCase();

        RouteKey key = new RouteKey(path, method);

        // 2. 查找处理器
        HandlerMethod handler = routeMappings.get(key);
        if (handler == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // TODO 执行前置拦截器

        // 4. 调用处理器方法
        try {
            Object result = handler.method.invoke(handler.controllerInstance, req, resp);

            // 5. 处理返回结果
            ServletUtils.sendResponse(resp, Result.success(result));

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private void handleError(HttpServletResponse resp, Exception e) throws IOException {

        Map<String, String> error = Map.of(
                "error", e.getClass().getSimpleName(),
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        );

        ServletUtils.sendResponse(resp, Result.error(objectMapper.writeValueAsString(error)));
    }
}