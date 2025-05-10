package com.anyview.xiazihao.controller;

import com.anyview.xiazihao.containerFactory.ContainerFactory;
import com.anyview.xiazihao.controller.annotation.*;
import com.anyview.xiazihao.entity.result.Result;
import com.anyview.xiazihao.sampleFlatMapper.KatSimpleMapper;
import com.anyview.xiazihao.sampleFlatMapper.TypeConverter;
import com.anyview.xiazihao.utils.JsonUtils;
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
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
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
        int bestMatchScore = -1; // 用于记录最佳匹配分数

        for (Map.Entry<RouteKey, HandlerMethod> entry : routeMappings.entrySet()) {
            RouteKey key = entry.getKey();
            if (key.httpMethod().equals(method) && key.matches(path)) {
                int currentScore = calculateMatchScore(key.pathPattern(), path);

                if (currentScore > bestMatchScore) {
                    bestMatchScore = currentScore;
                    matchedKey = key;
                    handler = entry.getValue();
                }
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

    // 计算路径匹配分数
    private int calculateMatchScore(String pattern, String path) {
        if (pattern.equals(path)) {
            return 100; // 精确匹配最高分
        }
        if (pattern.contains("{") && pattern.contains("}")) {
            return 50;  // 路径变量匹配中等分数
        }
        if (pattern.contains("*")) {
            return 10;  // 通配符匹配最低分
        }
        return 0;
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
            KatPathVariable pathVar = findAnnotation(paramAnnotations[i],KatPathVariable.class);
            if (pathVar != null) {
                args[i] = resolvePathVariable(pathVar, method.getParameters()[i],
                        paramTypes[i], req);
                continue;
            }

            // 处理 @KatRequestParam 注解参数
            KatRequestParam requestParam = findAnnotation(paramAnnotations[i], KatRequestParam.class); //TODO 也许能通过缓存来避免重复查找
            if (requestParam != null) {
                args[i] = resolveRequestParam(requestParam,method.getParameters()[i], paramTypes[i], req);
                continue;
            }

            // 处理 @KatRequestBody 注解参数
            KatRequestBody requestBody = findAnnotation(paramAnnotations[i], KatRequestBody.class);
            if (requestBody != null) {
                args[i] = resolveRequestBody(paramTypes[i], req, requestBody);
                continue;
            }

            // 处理 HttpServletRequest/HttpServletResponse 参数
            if (paramTypes[i].equals(HttpServletRequest.class)) {
                args[i] = req;
            } else if (paramTypes[i].equals(HttpServletResponse.class)) {
                args[i] = resp;
            }
        }

        return method.invoke(handler.controllerInstance(), args);
    }

    private Object resolveRequestBody(Class<?> paramType,
                                      HttpServletRequest req,
                                      KatRequestBody annotation) throws IOException {
        // 检查请求体是否为空
        if (req.getContentLength() == 0) {
            if (annotation.required()) {
                throw new IllegalArgumentException("Required request body is missing");
            }
            return null;
        }

        try {
            String requestBody = ServletUtils.getRequestBody(req);

            // 如果是String类型，直接返回
            if (paramType.equals(String.class)) {
                return requestBody;
            }

            // 构建对象
            return JsonUtils.parseJson(requestBody, paramType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse request body", e);
        }
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

    private Object resolveRequestParam(KatRequestParam annotation,
                                       java.lang.reflect.Parameter parameter,
                                       Class<?> paramType,
                                       HttpServletRequest req) {
        // 获取参数名，优先使用注解值，其次使用参数名
        String paramName = annotation.value().isEmpty()
                ? parameter.getName()
                : annotation.value();

        String paramValue = req.getParameter(paramName);
        if (TypeConverter.isSimpleType(paramType)) {
            // 处理参数缺失情况
            if (paramValue == null || paramValue.isEmpty()) {
                if (annotation.required()) {
                    throw new IllegalArgumentException("Required request parameter '" + paramName + "' is not present");
                }
                if (!annotation.defaultValue().isEmpty()) {
                    paramValue = annotation.defaultValue();
                } else {
                    return null; // 非必需且无默认值，返回null
                }
            }

            try {
                // 使用 TypeConverter 进行类型转换
                return TypeConverter.convertValue(paramValue, paramType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        String.format("Failed to convert request parameter '%s' value '%s' to type %s",
                                paramName, paramValue, paramType.getName()),
                        e
                );
            }
        } else {
            try {
                return bindObjectFromRequest(paramType, req);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to bind request parameters to " + paramType.getName(), e);
            }
        }
    }

    private Object bindObjectFromRequest(Class<?> targetClass, HttpServletRequest request) throws Exception {
        // 将请求参数转换为Map
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> params = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            // 对于多值参数，只取第一个值
            params.put(entry.getKey(), entry.getValue()[0]);
        }

        // 使用KatSimpleMapper的机制创建对象并绑定参数
        Constructor<?> constructor = targetClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();

        // 获取访问器缓存
        Map<String, MethodHandle[]> accessors = KatSimpleMapper.getAccessorCache()
                .computeIfAbsent(targetClass, KatSimpleMapper::createAccessors);

        // 绑定参数
        for (Map.Entry<String, MethodHandle[]> entry : accessors.entrySet()) {
            String fieldName = entry.getKey();
            MethodHandle setter = entry.getValue()[1]; // [1]是setter

            // 查找参数值
            Object value = params.get(fieldName);

            if (value != null) {
                try {
                    // 获取字段类型
                    Class<?> fieldType = setter.type().parameterType(1);
                    // 类型转换
                    Object convertedValue = TypeConverter.convertValue(value, fieldType);
                    // 设置值
                    setter.invoke(instance, convertedValue);
                } catch (Throwable e) {
                    throw new IllegalArgumentException(
                            String.format("Failed to set field '%s' with value '%s'",
                                    fieldName, value), e);
                }
            }
        }

        return instance;
    }



    @SuppressWarnings("unchecked")
    private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> annotationClass) {
        for (Annotation ann : annotations) {
            if (annotationClass.isInstance(ann)) {
                return (T) ann;
            }
        }
        return null;
    }



    private void handleError(HttpServletResponse resp, Exception e) throws IOException {

        Map<String, String> error = Map.of(
                "error", e.getClass().getSimpleName(),
                "message", e.getMessage() == null ? "Unknown error" : e.getMessage(),
                "timestamp", Instant.now().toString()
        );

        ServletUtils.sendResponse(resp, Result.error(objectMapper.writeValueAsString(error)));
        log.error(e.getMessage(), e);
    }
}