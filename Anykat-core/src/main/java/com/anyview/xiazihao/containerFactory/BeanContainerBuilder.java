package com.anyview.xiazihao.containerFactory;

import com.anyview.xiazihao.aspectProcessor.AspectInterceptor;
import com.anyview.xiazihao.aspectProcessor.AspectProcessor;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAspect;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;

@Slf4j
public class BeanContainerBuilder {
    private final BeanRegistry registry;
    private final AspectProcessor aspectProcessor; //aop处理器

    // 实例缓存和状态
    private final Map<String, Object> singletonInstances = new HashMap<>();
    private final Set<String> beansInCreation = new HashSet<>();

    // 依赖图
    private final Map<String, List<String>> dependencyGraph = new HashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();
    // 切面实例缓存
    private final Map<Class<?>, Object> aspectInstances = new ConcurrentHashMap<>();

    public BeanContainerBuilder(BeanRegistry registry) {
        this.registry = registry;
        this.aspectProcessor = new AspectProcessor();
    }

    public void build() {
        buildDependencyGraph();
        processAspects();
        initializeSingletons();
    }

    //拓扑排序
    private List<String> topologicalSort() {
        Queue<String> queue = new LinkedList<>();
        List<String> sortedBeans = new ArrayList<>();

        // 初始化队列(入度为0的Bean)
        for (String bean : inDegree.keySet()) {
            if (inDegree.get(bean) == 0) {
                queue.offer(bean);
            }
        }

        // 拓扑排序
        while (!queue.isEmpty()) {
            String bean = queue.poll();
            sortedBeans.add(bean);

            for (String dependent : dependencyGraph.getOrDefault(bean, Collections.emptyList())) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }

        Map<String, Class<?> > classRegistry = registry.getClassRegistry();
        // 检查循环依赖
        if (sortedBeans.size() != classRegistry.size()) {
            throw new RuntimeException("Circular dependency detected!");
        }

        return sortedBeans;
    }

    //  构建依赖图
    private void buildDependencyGraph() {
        // 初始化图和入度
        registry.getClassRegistry().keySet().forEach(beanName -> {
            dependencyGraph.put(beanName, new ArrayList<>());
            inDegree.put(beanName, 0);
        });

        // 构建依赖关系
        registry.getClassRegistry().forEach((beanName, clazz) -> {
            // 处理字段依赖
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(KatAutowired.class)) {
                    String dependencyName = registry.resolveBeanName(field.getType());
                    dependencyGraph.get(beanName).add(dependencyName);
                    inDegree.put(dependencyName, inDegree.get(dependencyName) + 1);
                }
            }

            // 处理构造器依赖
            Constructor<?> autowiredCtor = findAutowiredConstructor(clazz);
            if (autowiredCtor != null) {
                for (Class<?> paramType : autowiredCtor.getParameterTypes()) {
                    String dependencyName = registry.resolveBeanName(paramType);
                    dependencyGraph.get(beanName).add(dependencyName);
                    inDegree.put(dependencyName, inDegree.get(dependencyName) + 1);
                }
            }
        });
    }

    //处理aop注册
    private void processAspects() {
        registry.getAspectClasses().forEach(aspectClass -> {
            try {
                Object aspect = createAspectInstance(aspectClass);
                aspectInstances.put(aspectClass, aspect);
                aspectProcessor.registerAspect(aspect);
            } catch (Exception e) {
                throw new RuntimeException("Aspect initialization failed: " + aspectClass.getName(), e);
            }
        });
    }

    private Object createAspectInstance(Class<?> aspectClass) throws Exception {
        // 切面也需要依赖注入
        Object instance = createRawInstance(aspectClass);
        injectFields(instance);
        return instance;
    }

    // 创建原始实例（不经过AOP代理）
    private Object createRawInstance(Class<?> clazz) throws Exception {
        Constructor<?> autowiredCtor = findAutowiredConstructor(clazz);
        if (autowiredCtor != null) {
            return createInstanceWithConstructor(autowiredCtor);
        }
        return clazz.getDeclaredConstructor().newInstance();
    }


    private Object createProxy(Object target, Class<?> targetClass) throws Exception {
        if (targetClass.isInterface()) {
            return Proxy.newProxyInstance(
                    targetClass.getClassLoader(),
                    new Class<?>[]{targetClass},
                    (proxy, method, args) -> aspectProcessor.applyAspects(target, method, args)
            );
        } else {
            return new ByteBuddy()
                    .subclass(targetClass)
                    .method(not(isDeclaredBy(Object.class))) // 关键修改：在method()处过滤
                    .intercept(MethodDelegation.to(new AspectInterceptor(target, aspectProcessor)))
                    .make()
                    .load(targetClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        }
    }

    // 获取Bean实例
    @SuppressWarnings("unchecked") //忽略泛型警告
    public <T> T getBean(Class<T> interfaceType) {
        //接口模式
        Class<?> implementationClass = registry.getImplementation(interfaceType);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceType);
        }
        return (T) getBean(registry.getBeanName(implementationClass), implementationClass);
    }

    @SuppressWarnings("unchecked") //忽略泛型警告
    public <T> T getBean(String beanName, Class<T> clazz) {
        // 检查单例缓存
        if (singletonInstances.containsKey(beanName)) {
            return (T) singletonInstances.get(beanName);
        }
        Map<String, Class<?>> classRegistry = registry.getClassRegistry();
        // 检查是否已注册
        if (!classRegistry.containsKey(beanName)) {
            throw new RuntimeException("Bean not registered: " + beanName);
        }

        // 检查循环依赖
        if (beansInCreation.contains(beanName)) {
            throw new RuntimeException("Circular dependency detected for bean: " + beanName);
        }

        beansInCreation.add(beanName);
        try {
            Class<?> targetClass = registry.getClassRegistry().get(beanName);
            Object instance = createInstance(targetClass);

            // 如果是单例则缓存代理后的实例
            if (targetClass.isAnnotationPresent(KatSingleton.class)) {
                singletonInstances.put(beanName, instance);
            }

            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + beanName, e);
        } finally {
            beansInCreation.remove(beanName);
        }
    }

    //创建单例对象
    private void initializeSingletons() {
        List<String> sortedBeans = topologicalSort();
        sortedBeans.forEach(beanName -> {
            Class<?> clazz = registry.getClassRegistry().get(beanName);
            if (clazz.isAnnotationPresent(KatSingleton.class)) {
                getBean(beanName, clazz);
            }
        });
    }

    // 创建实例
    private Object createInstance(Class<?> clazz) throws Exception {
        // 1. 创建原始实例（不区分单例/原型）
        Object rawInstance;
        Constructor<?> autowiredCtor = findAutowiredConstructor(clazz);
        if (autowiredCtor != null) {
            rawInstance = createInstanceWithConstructor(autowiredCtor);
        } else {
            rawInstance = clazz.getDeclaredConstructor().newInstance();
        }

        // 2. 注入依赖
        injectFields(rawInstance);

        // 3. 统一应用AOP代理（无论是否单例）
        return wrapWithAopIfNeeded(rawInstance, clazz);
    }

    private Object wrapWithAopIfNeeded(Object rawInstance, Class<?> targetClass) {
        try {
            if (shouldProxy(targetClass)) {
                return createProxy(rawInstance, targetClass);
            }
            return rawInstance;
        } catch (Exception e) {
            throw new RuntimeException("AOP proxy creation failed for " + targetClass.getName(), e);
        }
    }

    private boolean shouldProxy(Class<?> targetClass) {
        // 不是切面类 && 有匹配的切面逻辑
        return !targetClass.isAnnotationPresent(KatAspect.class) &&
                aspectProcessor.hasMatchingAdvice(targetClass);
    }

    // 查找@KatAutowired构造器
    private Constructor<?> findAutowiredConstructor(Class<?> clazz) {
        Constructor<?>[] ctors = clazz.getConstructors();
        for (Constructor<?> ctor : ctors) {
            if (ctor.isAnnotationPresent(KatAutowired.class)) {
                return ctor;
            }
        }
        return null;
    }

    // 使用构造器创建实例
    private Object createInstanceWithConstructor(Constructor<?> ctor) throws Exception {
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] args = new Object[paramTypes.length]; //获取参数

        for (int i = 0; i < paramTypes.length; i++) { //添加参数
            args[i] = getBean(paramTypes[i]);
        }

        Object instance = ctor.newInstance(args); //创建实例
        injectFields(instance); //注入依赖字段
        return instance;
    }

    // 注入字段依赖
    private void injectFields(Object instance) throws IllegalAccessException {
        Class<?> clazz = instance.getClass();
        //遍历目标类的所有字段（包括私有字段）
        for (Field field : clazz.getDeclaredFields()) {
            // 检查字段是否被@KatAutowired注解标注
            if (field.isAnnotationPresent(KatAutowired.class)) {
                Object dependency = getBean(field.getType());
                field.setAccessible(true); //允许访问私有字段
                field.set(instance, dependency); //注入目标字段
            }
        }
    }

    //手动注入依赖
    public void injectDependencies(Object target) throws Exception {
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(KatAutowired.class)) {
                Object bean = getBean(field.getType()); // 从容器获取依赖
                field.setAccessible(true);
                log.debug(String.valueOf(target.getClass().getClassLoader()));
                log.debug(String.valueOf(bean.getClass().getClassLoader()));

                field.set(target, bean); // 注入到目标对象
            }
        }
    }
}