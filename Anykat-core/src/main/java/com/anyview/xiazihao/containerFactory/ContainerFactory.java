package com.anyview.xiazihao.containerFactory;

import com.anyview.xiazihao.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class ContainerFactory {
    private final BeanRegistry registry;
    private final BeanContainerBuilder builder;

    public ContainerFactory(String... basePackages) {
        log.debug("初始化扫描包 {}", (Object) basePackages);
        this.registry = new BeanRegistry(new HashSet<>(Arrays.asList(basePackages)));
        this.builder = new BeanContainerBuilder(registry);
        initialize();
    }

    public ContainerFactory() throws FileNotFoundException {
        this(AppConfig.getInstance().getContainer().getScanPackages().toArray(new String[0]));
    }

    private void initialize() {
        registry.scanAndRegister();  // 扫描和注册组件
        builder.build();             // 构建容器
    }

    // 委托给builder的方法
    public <T> T getBean(Class<T> type) {
        return builder.getBean(type);
    }

    public void injectDependencies(Object target) throws Exception {
        builder.injectDependencies(target);
    }
}