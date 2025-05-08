package com.anyview.xiazihao.containerFactory;

import com.anyview.xiazihao.config.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class ContainerFactory {
    @Getter
    private final BeanRegistry registry;
    @Getter
    private final BeanContainerBuilder builder;

    // 初始化锁
    private volatile boolean initialized = false;
    private final Object initLock = new Object();

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
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    registry.scanAndRegister();
                    builder.build();
                    initialized = true; // 标记为已初始化
                }
            }
        }
    }

    // 委托给builder的方法
    public <T> T getBean(Class<T> type) {
        return builder.getBean(type);
    }

    public void injectDependencies(Object target) throws Exception {
        builder.injectDependencies(target);
    }
}