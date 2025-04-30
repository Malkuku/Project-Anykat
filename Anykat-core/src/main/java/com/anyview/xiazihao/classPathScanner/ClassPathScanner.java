package com.anyview.xiazihao.classPathScanner;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class ClassPathScanner {

    /**
     * 扫描指定包下的所有类
     * @param packageName 包名 (如: com.example)
     * @param classFilter 类过滤器
     * @return 符合条件的类列表
     */
    public static List<Class<?>> scanClasses(String packageName, Predicate<Class<?>> classFilter) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            log.debug("ClassLoader: {}", classLoader);
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    classes.addAll(findClasses(new File(resource.getFile()), packageName, classFilter));
                }
                if (resource.getProtocol().equals("jar")) {
                    JarFile jar = ((JarURLConnection) resource.openConnection()).getJarFile();
                    classes.addAll(findClassesInJar(jar, path, packageName, classFilter));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("扫描类路径失败", e);
        }

        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName,
                                              Predicate<Class<?>> classFilter) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        log.debug("Scanning classes in package: {}",packageName);
        log.debug("Scanning classes in directory: {}",directory.getAbsolutePath());

        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName + "." + file.getName();
                classes.addAll(findClasses(file, subPackage, classFilter));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' +
                        file.getName().substring(0, file.getName().length() - 6);//获取class全类名
                Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader()); //根据全类名找到clazz对象(不对类进行初始化)
                if (classFilter.test(clazz)) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
    private static Set<Class<?>> findClassesInJar(JarFile jar, String path, String packageName,
                                                  Predicate<Class<?>> classFilter) throws ClassNotFoundException, IOException {
        Set<Class<?>> classes = new HashSet<>();
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            // 跳过目录和非class文件
            if (entry.isDirectory() || !entryName.endsWith(".class")) {
                continue;
            }

            // 检查是否在指定路径下
            if (!entryName.startsWith(path)) {
                continue;
            }

            // 转换为完全限定类名
            String className = entryName
                    .substring(0, entryName.length() - 6) // 去掉".class"
                    .replace('/', '.'); // 路径分隔符转换为包分隔符

            try {
                Class<?> clazz = Class.forName(className);
                if (classFilter.test(clazz)) {
                    classes.add(clazz);
                }
            } catch (NoClassDefFoundError e) {
                // 忽略无法加载的类
                System.err.println("无法加载类: " + className + ", 原因: " + e.getMessage());
            } catch (UnsupportedClassVersionError e) {
                // 忽略不支持的类版本
                System.err.println("不支持的类版本: " + className);
            }
        }

        return classes;
    }

    /**
     * 扫描带有指定注解的类
     * @param packageName 包名
     * @param annotation 注解类
     * @return 带有指定注解的类列表
     */
    public static List<Class<?>> scanClassesWithAnnotation(String packageName,
                                                           Class<? extends java.lang.annotation.Annotation> annotation) {
        return scanClasses(packageName, clazz -> clazz.isAnnotationPresent(annotation));
    }

    /**
     * 扫描实现指定接口的类
     * @param packageName 包名
     * @param interfaceType 接口类
     * @return 实现指定接口的类列表
     */
    public static List<Class<?>> scanClassesImplementing(String packageName,
                                                         Class<?> interfaceType) {
        return scanClasses(packageName, clazz ->
                interfaceType.isAssignableFrom(clazz) && !clazz.isInterface());
    }

    /**
     * 扫描继承指定父类的类
     * @param packageName 包名
     * @param superClass 父类
     * @return 继承指定父类的类列表
     */
    public static List<Class<?>> scanClassesExtending(String packageName,
                                                      Class<?> superClass) {
        return scanClasses(packageName, clazz ->
                superClass.isAssignableFrom(clazz) && !clazz.equals(superClass));
    }
}