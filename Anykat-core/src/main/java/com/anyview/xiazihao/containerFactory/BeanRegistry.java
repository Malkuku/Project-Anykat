package com.anyview.xiazihao.containerFactory;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.classPathScanner.ClassPathScanner;

import java.util.*;

//注册和元数据管理
public class BeanRegistry {
    // 类定义注册表
    private final Map<String, Class<?>> classRegistry = new HashMap<>();
    // 接口到实现类的映射
    private final Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();
    // 包扫描路径
    private final Set<String> basePackages;
    public BeanRegistry(Set<String> basePackages) {
        this.basePackages = basePackages;
    }

    public void scanAndRegister() {
        scanComponents();
        initializeInterfaceLinks();
    }

    //  扫描组件
    private void scanComponents() {
        List<Class<?>> componentClasses = new ArrayList<>();
        for (String basePackage : basePackages) {
            componentClasses.addAll(
                    ClassPathScanner.scanClassesWithAnnotation(basePackage, KatComponent.class)
            );
        }

        for (Class<?> clazz : componentClasses) {
            registerClass(clazz);
        }
    }

    //  注册类
    public void registerClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(KatComponent.class)) {
            String beanName = getBeanName(clazz);
            classRegistry.put(beanName, clazz);
        }
    }

    //设置接口到实现类的映射
    private void initializeInterfaceLinks() {
        for (Class<?> clazz : classRegistry.values()) {
            for (Class<?> intf : clazz.getInterfaces()) {
                if (!interfaceToImplementation.containsKey(intf)) {
                    interfaceToImplementation.put(intf, clazz);
                }
            }
        }
    }

    //  获取bean名称
    public String getBeanName(Class<?> clazz) {
        KatComponent component = clazz.getAnnotation(KatComponent.class);
        return component.value().isEmpty() ? clazz.getSimpleName() : component.value();
    }

    //  解析bean名称
    public String resolveBeanName(Class<?> type) {
        if (type.isInterface()) {
            Class<?> implementation = interfaceToImplementation.get(type);
            if (implementation == null) {
                throw new RuntimeException("No implementation for interface: " + type.getName());
            }
            return getBeanName(implementation);
        }
        return getBeanName(type);
    }

    // Getter方法
    public Map<String, Class<?>> getClassRegistry() {
        return Collections.unmodifiableMap(classRegistry);
    }

    public Class<?> getImplementation(Class<?> interfaceType) {
        return interfaceToImplementation.get(interfaceType);
    }


}