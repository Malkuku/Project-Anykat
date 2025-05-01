package com.anyview.xiazihao.beanRegistry;

import com.anyview.xiazihao.beanRegistry.Annotation.KatAutowired;
import com.anyview.xiazihao.beanRegistry.Annotation.KatComponent;
import com.anyview.xiazihao.beanRegistry.Annotation.KatSingleton;
import com.anyview.xiazihao.classPathScanner.ClassPathScanner;
import com.anyview.xiazihao.config.AppConfig;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class ContainerFactory {
    // 存储类定义的映射（类名 -> 类对象）
    private final Map<String, Class<?>> classRegistry = new HashMap<>();
    // 存储单例实例的映射（类名 -> 实例）
    private final Map<String, Object> singletonInstances = new HashMap<>();
    // 正在创建的Bean记录（用于解决循环依赖）
    private final Set<String> beansInCreation = new HashSet<>();
    //接口到实现类的映射
    private final Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();
    // 包扫描路径
    private final Set<String> basePackages;

    public ContainerFactory(String... basePackages) {
        this.basePackages = new HashSet<>(Arrays.asList(basePackages));
        scanComponents();
        initializeInterfaceLinks();
        initializeSingletons();
    }

    public ContainerFactory() throws FileNotFoundException {
        this.basePackages = AppConfig.getInstance().getContainer().getScanPackages();
        scanComponents();
        initializeInterfaceLinks();
        initializeSingletons();

    }

    // 扫描组件并注册
    private void scanComponents() {
        List<Class<?>> componentClasses = new ArrayList<>();
        for (String basePackage : basePackages) {
            componentClasses.addAll(ClassPathScanner.scanClassesWithAnnotation(
                    basePackage, KatComponent.class));
        }

        for (Class<?> clazz : componentClasses) {
            register(clazz);
        }
    }

    // 初始化所有单例Bean
    private void initializeSingletons() {
        for (Map.Entry<String, Class<?>> entry : classRegistry.entrySet()) {
            Class<?> clazz = entry.getValue();
            if (clazz.isAnnotationPresent(KatSingleton.class)) {
                getBean(clazz); // 触发单例初始化
            }
        }
    }

    // 注册组件
    public void register(Class<?> clazz) {
        if (clazz.isAnnotationPresent(KatComponent.class)) {
            String beanName = getBeanName(clazz);
            classRegistry.put(beanName, clazz);
        }
    }

    //手动注入依赖
    public void injectDependencies(Object target) throws Exception {
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(KatAutowired.class)) {
                Object bean = getBean(field.getType()); // 从容器获取依赖
                field.setAccessible(true);
                field.set(target, bean); // 注入到目标对象
            }
        }
    }

    //初始化接口索引
    private void initializeInterfaceLinks() {
        for (Class<?> clazz : classRegistry.values()) {
            for (Class<?> intf : clazz.getInterfaces()) {
                if (!interfaceToImplementation.containsKey(intf)) {
                    interfaceToImplementation.put(intf, clazz);
                }
            }
        }
    }

    // 获取Bean名称
    private String getBeanName(Class<?> clazz) {
        KatComponent component = clazz.getAnnotation(KatComponent.class);
        return component.value().isEmpty() ? clazz.getSimpleName() : component.value(); //注解没有指定Bean名称时，以类名作为Bean名称
    }

    // 获取Bean实例
    @SuppressWarnings("unchecked") //忽略泛型警告
    public <T> T getBean(Class<T> interfaceType) {
        //接口模式
        Class<?> implementationClass = interfaceToImplementation.get(interfaceType);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceType);
        }
        return (T) getBean(getBeanName(implementationClass), implementationClass);
    }

    @SuppressWarnings("unchecked") //忽略泛型警告
    public <T> T getBean(String beanName, Class<T> clazz) {
        // 检查单例缓存
        if (singletonInstances.containsKey(beanName)) {
            return (T) singletonInstances.get(beanName);
        }

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
            Class<?> targetClass = classRegistry.get(beanName);
            Object instance = createInstance(targetClass); //创建实例

            // 如果是单例则缓存
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

    // 创建实例
    private Object createInstance(Class<?> clazz) throws Exception {
        // 1. 优先使用@KatAutowired构造器
        Constructor<?> autowiredCtor = findAutowiredConstructor(clazz);
        if (autowiredCtor != null) {
            return createInstanceWithConstructor(autowiredCtor);
        }

        // 2. 使用默认无参构造器
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance(); //获取无参构造，并创建实例
            injectFields(instance); //注入依赖字段
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No suitable constructor found for " + clazz.getName());
        }
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
}