package com.anyview.xiazihao.sampleFlatMapper;

import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;

@Slf4j
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

    // 命名策略枚举
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
            MethodHandle constructorHandle = MethodHandles.lookup().unreflectConstructor(constructor);// 使用MethodHandle包装构造器，比传统反射调用性能更高

            // 从缓存获取或创建字段信息
            Map<String, MethodHandle> setters = Cache.SETTER_CACHE
                    .computeIfAbsent(targetClass, KatSimpleMapper::createSetters);

            Map<String, Class<?>> fieldTypes = Cache.FIELD_TYPE_CACHE
                    .computeIfAbsent(targetClass, KatSimpleMapper::getFieldTypes);

            return (rs, index) -> {
                try {
                    @SuppressWarnings("unchecked")
                    T instance = (T) constructorHandle.invoke();//创建目标对象实例

                    for (Map.Entry<String, MethodHandle> entry : setters.entrySet()) {
                        String fieldName = entry.getKey();
                        String columnName = namingStrategy.convert(fieldName);

                        try {
                            Object value = rs.getObject(columnName);
                            if (value != null) {
                                Class<?> fieldType = fieldTypes.get(fieldName);
                                Object convertedValue = convertType(value, fieldType);//类型转换
                                entry.getValue().invoke(instance, convertedValue);//设置属性值
                            }
                        } catch (SQLException e) {
                            // 列不存在时跳过
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
            MethodHandles.Lookup lookup = MethodHandles.lookup();// 获取MethodHandles.Lookup实例，用于方法/字段查找

            for (Field field : targetClass.getDeclaredFields()) {
                try {
                    MethodHandle setter = lookup.unreflectSetter(field);// 尝试通过标准setter方法获取MethodHandle
                    setters.put(field.getName(), setter);
                } catch (IllegalAccessException e) {
                    // 如果字段没有setter，尝试直接设置字段值
                    field.setAccessible(true);
                    MethodHandle setter = lookup.unreflectSetter(field);
                    setters.put(field.getName(), setter);
                }
            }
            return Collections.unmodifiableMap(setters); // 返回不可修改的Map
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

    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 提取 SQL 参数（支持实体类、Map、基本变量）
     * @param sql SQL 语句，如 "SELECT * FROM user WHERE id = #{id}"
     * @param params 参数（可以是实体类、Map，或显式键值对如 "id", 123）
     */
    public static Object[] extractParams(String sql, Object... params) {
        List<String> paramNames = extractNamedParamNames(sql);
        List<Object> values = new ArrayList<>();

        for (String paramName : paramNames) {
            Object value = findParamValue(paramName, params);
            if (value == null && !containsParam(paramName, params)) {
                throw new IllegalArgumentException("Parameter '" + paramName + "' not found");
            }
            values.add(value);
        }

        return values.toArray();
    }



    /**
     * 从混合参数中查找值（优先级：Map > 键值对 > 实体类字段）
     */
    private static Object findParamValue(String paramName, Object[] params) {
        // 第一阶段：查找 Map 中的参数（最高优先级）
        for (Object param : params) {
            if (param instanceof Map<?, ?> map) {
                if (map.containsKey(paramName)) {
                    return map.get(paramName);
                }
            }
        }

        // 第二阶段：查找显式键值对（如 "id", 123）
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String key && i + 1 < params.length) {
                if (key.equals(paramName)) {
                    return params[i + 1];
                }
            }
        }

        // 第三阶段：查找实体类字段（最低优先级）
        for (Object param : params) {
            if (param == null || param instanceof Map || param instanceof String) {
                continue; // 跳过 Map 和键值对
            }
            Class<?> clazz = param.getClass();
            cacheEntityFields(clazz);
            Map<String, Field> fieldMap = FIELD_CACHE.get(clazz);
            if (fieldMap != null) {
                Field field = fieldMap.get(paramName.toLowerCase());
                if (field != null) {
                    try {
                        field.setAccessible(true);
                        return field.get(param);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access field: " + paramName, e);
                    }
                }
            }
        }

        return null; // 未找到
    }

    /**
     * 检查参数是否存在于任意实体中
     */
    private static boolean containsParam(String paramName, Object[] entities) {
        for (Object entity : entities) {
            if (entity == null) continue;
            Map<String, Field> fieldMap = FIELD_CACHE.get(entity.getClass());
            if (fieldMap != null && fieldMap.containsKey(paramName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 缓存实体类的所有字段（包括父类字段）
     */
    private static void cacheEntityFields(Class<?> clazz) {
        if (FIELD_CACHE.containsKey(clazz)) {
            return; // 已缓存
        }

        Map<String, Field> fieldMap = new HashMap<>();
        // 递归获取所有字段（包括父类）
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                String fieldName = field.getName();
                // 原始名称（小写）
                fieldMap.put(fieldName.toLowerCase(), field);
                // 驼峰转下划线格式（如果不同）
                String snakeCase = camelToSnake(fieldName);
                if (!snakeCase.equals(fieldName)) {
                    fieldMap.put(snakeCase.toLowerCase(), field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        FIELD_CACHE.put(clazz, fieldMap);
    }

    /**
     * 从SQL中提取#{paramName}格式的参数名
     */
    private static List<String> extractNamedParamNames(String sql) {
        List<String> paramNames = new ArrayList<>();
        Matcher matcher = Pattern.compile("#\\{(\\w+)}").matcher(sql);
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        return paramNames;
    }

    /**
     * 将#{param}替换为?
     */
    public static String replaceParamPlaceholders(String sql) {
        return sql.replaceAll("#\\{\\w+}", "?");
    }

    /**
     * 驼峰转下划线
     */
    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}