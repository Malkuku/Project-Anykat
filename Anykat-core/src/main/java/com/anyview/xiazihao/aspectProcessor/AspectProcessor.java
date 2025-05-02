package com.anyview.xiazihao.aspectProcessor;

import com.anyview.xiazihao.aspectProcessor.annotation.KatAround;
import com.anyview.xiazihao.aspectProcessor.annotation.KatOrder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AspectProcessor {
    // 缓存切点表达式与对应通知方法的映射
    private final Map<String, List<AdviceWrapper>> aspectCache = new ConcurrentHashMap<>();

    // 缓存已编译的正则表达式
    private final Map<String, Pattern> compiledPatterns = new ConcurrentHashMap<>();

    // 注册切面类
    public void registerAspect(Object aspect) {
        Class<?> aspectClass = aspect.getClass();
        Arrays.stream(aspectClass.getMethods())
                .filter(m -> m.isAnnotationPresent(KatAround.class))
                .forEach(method -> registerAdvice(aspect, method));
    }

    // 批量注册切面
    public void registerAspects(Collection<Object> aspectBeans) {
        aspectBeans.forEach(this::registerAspect);
    }

    // 清空缓存
    public void clearCache() {
        aspectCache.clear();
        compiledPatterns.clear();
    }

    // 注册单个通知方法
    private void registerAdvice(Object aspect, Method adviceMethod) {
        KatAround around = adviceMethod.getAnnotation(KatAround.class);
        String pointcut = around.value();
        int priority = adviceMethod.isAnnotationPresent(KatOrder.class)
                ? adviceMethod.getAnnotation(KatOrder.class).value() : 0;

        // 编译切点表达式
        compiledPatterns.computeIfAbsent(pointcut, this::compilePointcut);

        // 注册通知方法并按优先级排序
        aspectCache.computeIfAbsent(pointcut, k -> new CopyOnWriteArrayList<>())
                .add(new AdviceWrapper(aspect, adviceMethod, priority));

        aspectCache.get(pointcut).sort(Comparator.comparingInt(AdviceWrapper::priority));
    }

    // 编译切点表达式为正则
    private Pattern compilePointcut(String expression) {
        String regex = convertToRegex(expression);
        return Pattern.compile(regex);
    }

    // 转换切点表达式为正则
    private static String convertToRegex(String pointcut) {
        // 先处理特殊字符转义（但不处理通配符）
        StringBuilder regex = new StringBuilder();
        char[] chars = pointcut.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // 处理..通配符（需要看后续字符）
            if (c == '.' && i + 1 < chars.length && chars[i + 1] == '.') {
                regex.append("\\..+?"); // 非贪婪匹配
                i++; // 跳过下一个点
                continue;
            }

            // 处理普通字符转义
            switch (c) {
                case '.':
                    regex.append("\\.");
                    break;
                case '$':
                    regex.append("\\$");
                    break;
                case '*':
                    // 临时标记，后面统一处理
                    regex.append("\u0001");
                    break;
                case '(':
                    // 参数部分临时标记
                    regex.append(handleParameters(chars, i));
                    i = skipParameters(chars, i);
                    break;
                default:
                    regex.append(c);
            }
        }

        // 最后处理*通配符（避免被前面替换影响）
        String result = regex.toString().replace("\u0001", "[^.]+");
        return "^" + result + "$";
    }

    // 处理参数部分
    private static String handleParameters(char[] chars, int start) {
        int end = findParameterEnd(chars, start);
        String params = new String(chars, start, end - start + 1);

        return switch (params) {
            case "(..)" -> "\\(.*\\)";
            case "()" -> "\\(\\)";
            case "(*)" -> "\\([^,]+\\)";
            default -> Pattern.quote(params); // 其他参数形式原样匹配
        };
    }

    // 跳过参数部分
    private static int skipParameters(char[] chars, int start) {
        return findParameterEnd(chars, start);
    }

    // 找到参数结束位置
    private static int findParameterEnd(char[] chars, int start) {
        if (chars[start] != '(') return start;
        int depth = 1;
        for (int i = start + 1; i < chars.length; i++) {
            if (chars[i] == '(') depth++;
            if (chars[i] == ')') depth--;
            if (depth == 0) return i;
        }
        return chars.length - 1;
    }

    // 检查类是否有匹配的切面
    public boolean hasMatchingAdvice(Class<?> targetClass) {
        return aspectCache.keySet().stream()
                .anyMatch(pointcut -> {
                    // 构建类名模式：com.example.Service -> com.example.Service.*(..)
                    String classPattern = targetClass.getName() + ".*(..)";
                    return matchesPointcut(pointcut, classPattern);
                });
    }

    // 检查方法是否有匹配的切面
    public boolean hasMatchingAdvice(Method method) {
        String methodSignature = buildMethodSignature(method);
        return aspectCache.keySet().stream()
                .anyMatch(pointcut -> matchesPointcut(pointcut, methodSignature));
    }

    // 构建完整方法签名
    private String buildMethodSignature(Method method) {
        String params = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName) // 使用简单类名
                .collect(Collectors.joining(","));
        return method.getDeclaringClass().getName() + "." + method.getName() + "(" + params + ")";
    }

    // 执行切面逻辑
    public Object applyAspects(Object target, Method method, Object[] args) throws Throwable {
        // 构建更精确的方法签名
        String methodSignature = target.getClass().getName() + "." + method.getName() +
                "(" + Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(",")) + ")";

        // 查找匹配的切面
        List<AdviceWrapper> matchedAdvices = aspectCache.entrySet().stream()
                .filter(entry -> matchesPointcut(entry.getKey(), methodSignature))
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparingInt(AdviceWrapper::priority))
                .toList();

        if (matchedAdvices.isEmpty()) {
            return method.invoke(target, args);
        }

        // 创建连接点
        ProceedingJoinPoint joinPoint = new ProceedingJoinPoint(target, method, args);

        // 执行通知链
        Object result = null;
        for (AdviceWrapper advice : matchedAdvices) {
            result = advice.invoke(joinPoint);
        }
        return result;
    }

    // 检查切点是否匹配签名
    private boolean matchesPointcut(String pointcut, String signature) {
        Pattern pattern = compiledPatterns.get(pointcut);
        if (pattern == null) {
            pattern = compilePointcut(pointcut);
            compiledPatterns.put(pointcut, pattern);
        }
        return pattern.matcher(signature).matches();
    }

    // 通知包装类
    private static class AdviceWrapper {
        private final Object aspectInstance;
        private final Method adviceMethod;
        private final int priority;

        public AdviceWrapper(Object aspectInstance, Method adviceMethod, int priority) {
            this.aspectInstance = aspectInstance;
            this.adviceMethod = adviceMethod;
            this.priority = priority;
            this.adviceMethod.setAccessible(true);
        }

        public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
            return adviceMethod.invoke(aspectInstance, joinPoint);
        }

        public int priority() {
            return priority;
        }
    }
}