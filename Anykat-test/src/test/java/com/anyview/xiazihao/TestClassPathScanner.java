package com.anyview.xiazihao;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.classPathScanner.ClassPathScanner;
import org.junit.Test;

import java.util.List;

public class TestClassPathScanner {
    @Test
    public void testScanClassesWithAnnotation() {
        List<Class<?>> annotationClasses = ClassPathScanner.scanClassesWithAnnotation(
                "com.anyview.xiazihao", KatComponent.class);
        annotationClasses.forEach(System.out::println);
    }
}
