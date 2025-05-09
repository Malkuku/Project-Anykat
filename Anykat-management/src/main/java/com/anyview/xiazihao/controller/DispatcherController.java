package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.ContainerFactory;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatPathVariable;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.entity.result.Result;
import com.anyview.xiazihao.sampleFlatMapper.TypeConverter;
import com.anyview.xiazihao.utils.PathUtils;
import com.anyview.xiazihao.utils.ServletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
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
    private record RouteKey(String pathPattern, String httpMethod) {
        private RouteKey(String pathPattern, String httpMethod) {
            this.pathPattern = PathUtils.normalize(pathPattern);
            this.httpMethod = httpMethod.toUpperCase();
        }

        // 添加路径匹配方法
        public boolean matches(String path) {
            return PathUtils.match(this.pathPattern, path);
        }

        // 添加路径变量提取方法
        public Map<String, String> extractPathVariables(String path) {
            return PathUtils.extractPathVariables(this.pathPattern, path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RouteKey routeKey = (RouteKey) o;
            return pathPattern.equals(routeKey.pathPattern) &&
                    httpMethod.equals(routeKey.httpMethod);
        }

        @Override
        public String toString() {
            return httpMethod + " " + pathPattern;
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

        // 查找匹配的路由
        RouteKey matchedKey = null;
        HandlerMethod handler = null;

        for (Map.Entry<RouteKey, HandlerMethod> entry : routeMappings.entrySet()) {
            RouteKey key = entry.getKey();
            if (key.httpMethod().equals(method) && key.matches(path)) {
                matchedKey = key;
                handler = entry.getValue();
                break;
            }
        }
        if (handler == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 提取路径参数
        Map<String, String> pathVariables = matchedKey.extractPathVariables(path);
        req.setAttribute("pathVariables", pathVariables);

        // TODO 执行前置拦截器

        try {
            // 调用处理器方法
            Object result = invokeHandlerMethod(handler, req, resp);

            // 5. 处理返回结果
            ServletUtils.sendResponse(resp, Result.success(result));

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private Object invokeHandlerMethod(HandlerMethod handler,
                                       HttpServletRequest req,
                                       HttpServletResponse resp) throws Exception {
        Method method = handler.method();
        Object[] args = new Object[method.getParameterCount()];
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramTypes.length; i++) {
            // 处理 @KatPathVariable 注解参数
            KatPathVariable pathVar = findAnnotation(paramAnnotations[i]);
            if (pathVar != null) {
                args[i] = resolvePathVariable(pathVar, method.getParameters()[i],
                        paramTypes[i], req);
                continue;
            }

            // 处理 HttpServletRequest/HttpServletResponse 参数
            if (paramTypes[i].equals(HttpServletRequest.class)) {
                args[i] = req;
            } else if (paramTypes[i].equals(HttpServletResponse.class)) {
                args[i] = resp;
            }
            // 可以添加其他参数类型的支持
        }

        return method.invoke(handler.controllerInstance(), args);
    }

    private Object resolvePathVariable(KatPathVariable annotation,
                                       java.lang.reflect.Parameter parameter,
                                       Class<?> paramType,
                                       HttpServletRequest req) {
        Map<String, String> pathVariables = (Map<String, String>) req.getAttribute("pathVariables");

        // 获取参数名，优先使用注解值，其次使用参数名
        String paramName = annotation.value().isEmpty()
                ? parameter.getName()
                : annotation.value();

        String value = pathVariables.get(paramName);
        if (value == null) {
            throw new IllegalArgumentException("Path variable '" + paramName + "' not found");
        }

        try {
            // 使用 TypeConverter 进行类型转换
            return TypeConverter.convertValue(value, paramType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Failed to convert path variable '%s' value '%s' to type %s",
                            paramName, value, paramType.getName()),
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> T findAnnotation(Annotation[] annotations) {
        for (Annotation ann : annotations) {
            if (ann instanceof KatPathVariable) {
                return (T) ann;
            }
        }
        return null;
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