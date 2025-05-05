package com.anyview.xiazihao.sampleFlatMapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class KatSimpleMapper {
    // 使用静态内部类实现线程安全的缓存
    private static class Cache {
        // 映射器缓存 (Class -> MapperFunction)
        static final Map<Class<?>, BiFunction<ResultSet, Integer, ?>> MAPPER_CACHE = new ConcurrentHashMap<>();

        // 字段类型缓存 (Class -> (FieldName -> FieldType))
        static final Map<Class<?>, Map<String, Class<?>>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

        // setter方法句柄缓存 (Class -> (FieldName -> MethodHandle))
        static final Map<Class<?>, Map<String, MethodHandle>> SETTER_CACHE = new ConcurrentHashMap<>();
    }

    // 命名策略枚举保持不变
    public enum NamingStrategy {
        SNAKE_CASE {
            @Override
            String convert(String fieldName) {
                return fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
            }
        },
        CAMEL_CASE {
            @Override
            String convert(String fieldName) {
                return fieldName;
            }
        };
        abstract String convert(String fieldName);
    }

    // 默认命名策略
    private static NamingStrategy namingStrategy = NamingStrategy.SNAKE_CASE;

    // 私有构造器防止实例化
    private KatSimpleMapper() {}

    /**
     * 设置全局命名策略
     */
    public static void setNamingStrategy(NamingStrategy strategy) {
        namingStrategy = Objects.requireNonNull(strategy);
    }

    /**
     * 映射结果集到对象列表 (静态方法)
     */
    public static <T> List<T> map(ResultSet rs, Class<T> targetClass) throws SQLException {
        @SuppressWarnings("unchecked")
        BiFunction<ResultSet, Integer, T> mapper = (BiFunction<ResultSet, Integer, T>)
                Cache.MAPPER_CACHE.computeIfAbsent(targetClass, KatSimpleMapper::createMapper);

        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapper.apply(rs, 1));
        }
        return results;
    }

    /**
     * 创建映射函数 (带缓存)
     */
    private static <T> BiFunction<ResultSet, Integer, T> createMapper(Class<T> targetClass) {
        try {
            // 获取或创建构造函数句柄
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            MethodHandle constructorHandle = MethodHandles.lookup().unreflectConstructor(constructor);

            // 从缓存获取或创建字段信息
            Map<String, MethodHandle> setters = Cache.SETTER_CACHE
                    .computeIfAbsent(targetClass, KatSimpleMapper::createSetters);

            Map<String, Class<?>> fieldTypes = Cache.FIELD_TYPE_CACHE
                    .computeIfAbsent(targetClass, KatSimpleMapper::getFieldTypes);

            return (rs, index) -> {
                try {
                    @SuppressWarnings("unchecked")
                    T instance = (T) constructorHandle.invoke();

                    for (Map.Entry<String, MethodHandle> entry : setters.entrySet()) {
                        String fieldName = entry.getKey();
                        String columnName = namingStrategy.convert(fieldName);

                        try {
                            Object value = rs.getObject(columnName);
                            if (value != null) {
                                Class<?> fieldType = fieldTypes.get(fieldName);
                                Object convertedValue = convertType(value, fieldType);
                                entry.getValue().invoke(instance, convertedValue);
                            }
                        } catch (SQLException e) {
                            // 列不存在时跳过
                            continue;
                        }
                    }
                    return instance;
                } catch (Throwable e) {
                    throw new RuntimeException("Mapping failed for " + targetClass.getName(), e);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Mapper creation failed for " + targetClass.getName(), e);
        }
    }

    /**
     * 创建setter方法句柄映射 (带缓存)
     */
    private static <T> Map<String, MethodHandle> createSetters(Class<T> targetClass) {
        try {
            Map<String, MethodHandle> setters = new HashMap<>();
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            for (Field field : targetClass.getDeclaredFields()) {
                try {
                    MethodHandle setter = lookup.unreflectSetter(field);
                    setters.put(field.getName(), setter);
                } catch (IllegalAccessException e) {
                    // 如果字段没有setter，尝试直接设置字段值
                    field.setAccessible(true);
                    MethodHandle setter = lookup.unreflectSetter(field);
                    setters.put(field.getName(), setter);
                }
            }
            return Collections.unmodifiableMap(setters);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create setters for " + targetClass.getName(), e);
        }
    }

    /**
     * 获取字段类型映射 (带缓存)
     */
    private static <T> Map<String, Class<?>> getFieldTypes(Class<T> targetClass) {
        Map<String, Class<?>> fieldTypes = new HashMap<>();
        for (Field field : targetClass.getDeclaredFields()) {
            fieldTypes.put(field.getName(), field.getType());
        }
        return Collections.unmodifiableMap(fieldTypes);
    }

    /**
     * 类型转换方法
     */
    private static Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        // 数值类型转换
        if (value instanceof Number number) {
            if (targetType == Double.class || targetType == double.class) {
                return number.doubleValue();
            }
            if (targetType == Float.class || targetType == float.class) {
                return number.floatValue();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return number.intValue();
            }
            if (targetType == Long.class || targetType == long.class) {
                return number.longValue();
            }
            if (targetType == Short.class || targetType == short.class) {
                return number.shortValue();
            }
        }

        // 日期类型转换
        if (value instanceof java.sql.Date sqlDate) {
            if (targetType == LocalDate.class) {
                return sqlDate.toLocalDate();
            }
            if (targetType == LocalDateTime.class) {
                return sqlDate.toLocalDate().atStartOfDay();
            }
        }

        // 布尔类型转换
        if (value instanceof Boolean bool) {
            if (targetType == Integer.class || targetType == int.class) {
                return bool ? 1 : 0;
            }
            if (targetType == Double.class || targetType == double.class) {
                return bool ? 1.0 : 0.0;
            }
        }

        // 时间戳转换
        if (value instanceof Timestamp timestamp) {
            if (targetType == LocalDateTime.class) {
                return timestamp.toLocalDateTime();
            }
            if (targetType == LocalDate.class) {
                return timestamp.toLocalDateTime().toLocalDate();
            }
        }

        return value;
    }

    /**
     * 清除所有缓存
     */
    public static void clearCache() {
        Cache.MAPPER_CACHE.clear();
        Cache.FIELD_TYPE_CACHE.clear();
        Cache.SETTER_CACHE.clear();
    }

    /**
     * 清除指定类的缓存
     */
    public static void clearCache(Class<?> targetClass) {
        Cache.MAPPER_CACHE.remove(targetClass);
        Cache.FIELD_TYPE_CACHE.remove(targetClass);
        Cache.SETTER_CACHE.remove(targetClass);
    }
}