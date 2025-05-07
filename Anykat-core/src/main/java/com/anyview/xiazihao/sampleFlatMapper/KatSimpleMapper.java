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

        //缓存访问器数组 [getter, setter]
        static final Map<Class<?>, Map<String, MethodHandle[]>> ACCESSOR_CACHE = new ConcurrentHashMap<>();

        //字段缓存
        private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

        //sql模板缓存
        private static final Map<String, SqlTemplate> SQL_TEMPLATE_CACHE = new ConcurrentHashMap<>();

        private static final Pattern PARAM_PATTERN = Pattern.compile("#\\{(\\w+)}");
    }

    private static final class SqlTemplate {
        final String originalSql;      // 原始SQL，如 "SELECT * FROM user WHERE id = #{id}"
        final String preparedSql;      // 预处理SQL，如 "SELECT * FROM user WHERE id = ?"
        final List<String> paramNames; // 参数名列表，如 ["id"]

        SqlTemplate(String sql) {
            this.originalSql = sql;
            this.paramNames = Collections.unmodifiableList(extractNamedParamNames(sql));
            this.preparedSql = replaceParamPlaceholders(sql);
        }
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
            T instance = mapper.apply(rs, 1);
            if (instance != null) {  // 只添加非 null 值
                results.add(instance);
            }
        }
        return results;
    }

    /**
     * 创建映射函数 (带缓存)
     */
    private static <T> BiFunction<ResultSet, Integer, T> createMapper(Class<T> targetClass) {
        // 处理不可变类型（如 Integer、String 等）
        if (isImmutableType(targetClass)) {
            return createImmutableMapper(targetClass);
        }

        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            MethodHandle constructorHandle = MethodHandles.lookup().unreflectConstructor(constructor);

            // 使用新的 ACCESSOR_CACHE
            Map<String, MethodHandle[]> accessors = Cache.ACCESSOR_CACHE
                    .computeIfAbsent(targetClass, KatSimpleMapper::createAccessors);

            Map<String, Class<?>> fieldTypes = Cache.FIELD_TYPE_CACHE
                    .computeIfAbsent(targetClass, KatSimpleMapper::getFieldTypes);

            return (rs, index) -> {
                try {
                    @SuppressWarnings("unchecked")
                    T instance = (T) constructorHandle.invoke();

                    for (Map.Entry<String, MethodHandle[]> entry : accessors.entrySet()) {
                        String fieldName = entry.getKey();
                        MethodHandle setter = entry.getValue()[1]; // [1]是setter
                        String columnName = namingStrategy.convert(fieldName);

                        try {
                            Object value = rs.getObject(columnName);
                            if (value != null) {
                                Class<?> fieldType = fieldTypes.get(fieldName);
                                Object convertedValue = convertType(value, fieldType);
                                setter.invoke(instance, convertedValue);
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
     * 判断是否为不可变类型
     */
    private static boolean isImmutableType(Class<?> clazz) {
        return clazz == Integer.class || clazz == int.class ||
                clazz == Long.class || clazz == long.class ||
                clazz == Double.class || clazz == double.class ||
                clazz == Float.class || clazz == float.class ||
                clazz == Short.class || clazz == short.class ||
                clazz == Byte.class || clazz == byte.class ||
                clazz == Boolean.class || clazz == boolean.class ||
                clazz == Character.class || clazz == char.class ||
                clazz == String.class;
    }

    /**
     * 创建不可变类型的映射器
     */
    private static <T> BiFunction<ResultSet, Integer, T> createImmutableMapper(Class<T> targetClass) {
        return (rs, index) -> {
            try {
                Object value = rs.getObject(index);
                if (value == null) {
                    return null;
                }
                return (T) convertType(value, targetClass);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to map immutable type: " + targetClass.getName(), e);
            }
        };
    }

    /**
     * 创建并缓存setter和getter方法句柄
     */
    private static <T> Map<String, MethodHandle[]> createAccessors(Class<T> targetClass) {
        try {
            Map<String, MethodHandle[]> accessors = new HashMap<>();
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            for (Field field : targetClass.getDeclaredFields()) {
                String fieldName = field.getName();
                MethodHandle[] handlers = new MethodHandle[2]; // [0]=getter, [1]=setter

                try {
                    // 优先尝试通过标准getter/setter方法获取
                    String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method getterMethod = targetClass.getMethod("get" + capitalized);
                    handlers[0] = lookup.unreflect(getterMethod);

                    try {
                        Method setterMethod = targetClass.getMethod("set" + capitalized, field.getType());
                        handlers[1] = lookup.unreflect(setterMethod);
                    } catch (NoSuchMethodException e) {
                        // 没有标准setter，使用字段直接访问
                        field.setAccessible(true);
                        handlers[1] = lookup.unreflectSetter(field);
                    }
                } catch (NoSuchMethodException e) {
                    // 没有标准getter/setter，直接访问字段
                    field.setAccessible(true);
                    handlers[0] = lookup.unreflectGetter(field);
                    handlers[1] = lookup.unreflectSetter(field);
                }

                accessors.put(fieldName, handlers);
            }

            return Collections.unmodifiableMap(accessors);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create accessors for " + targetClass.getName(), e);
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
        if (value == null){
            if(targetType == Number.class){ // Number类型检查
                return 0;
            }else return null;
        }
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
        log.warn("Unknown type to convert{} ", value);
        return value;
    }

    /**
     * 清除所有缓存
     */
    public static void clearCache() {
        Cache.MAPPER_CACHE.clear();
        Cache.FIELD_TYPE_CACHE.clear();
        Cache.ACCESSOR_CACHE.clear();
        Cache.FIELD_CACHE.clear();
        Cache.SQL_TEMPLATE_CACHE.clear();
    }

    /**
     * 清除指定类的缓存
     */
    public static void clearCache(Class<?> targetClass) {
        Cache.MAPPER_CACHE.remove(targetClass);
        Cache.FIELD_TYPE_CACHE.remove(targetClass);
        Cache.ACCESSOR_CACHE.remove(targetClass);
        Cache.FIELD_CACHE.remove(targetClass);
    }

    /**
     * 清除指定SQL模板缓存
     */
    public static void removeSqlTemplate(String sql) {
        Cache.SQL_TEMPLATE_CACHE.remove(sql);
    }


    /**
     * 提取 SQL 参数（支持实体类、Map、基本变量）
     * @param sql SQL 语句，如 "SELECT * FROM user WHERE id = #{id}"
     * @param params 参数（可以是实体类、Map，或显式键值对如 "id", 123）
     */
    public static Object[] extractParams(String sql, Object... params) {
        SqlTemplate template = Cache.SQL_TEMPLATE_CACHE.computeIfAbsent(sql,SqlTemplate::new);
        Object[] result = new Object[template.paramNames.size()];

        for (int i = 0; i < template.paramNames.size(); i++) {
            String paramName = template.paramNames.get(i);
            result[i] = findParamValue(paramName, params);
        }
        return result;
    }


    /**
     * 从混合参数中查找值（优先级：键值对 > Map > 实体类字段）
     */
    private static Object findParamValue(String paramName, Object[] params) {
        // 1：查找显式键值对（如 "id", 123）
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String key && i + 1 < params.length) {
                if (key.equals(paramName)) {
                    return params[i + 1];
                }
            }
        }

        // 2：查找 Map 中的参数
        for (Object param : params) {
            if (param instanceof Map<?, ?> map) {
                if (map.containsKey(paramName)) {
                    return map.get(paramName);
                }
            }
        }

        // 3：查找实体类字段
        for (Object param : params) {
            if (param == null || param instanceof Map || param instanceof String) {
                continue;
            }

            Class<?> clazz = param.getClass();
            Map<String, MethodHandle[]> accessors = Cache.ACCESSOR_CACHE.computeIfAbsent(clazz,KatSimpleMapper::createAccessors);
            MethodHandle getter;
            try{
                getter = accessors.get(paramName)[0]; // [0]是getter
            } catch (Exception e) {
                log.error("Failed to access field: {}", paramName);
                log.error("Check accessors: {}", accessors);
                throw new RuntimeException(e);
            }

            if (getter != null) {
                try {
                    return getter.invoke(param);
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to access field: " + paramName, e);
                }
            }
        }
        // 未找到
        return null;
    }

    /**
     * 检查参数是否存在于任意实体中
     */
    private static boolean containsParam(String paramName, Object[] entities) {
        for (Object entity : entities) {
            if (entity == null) continue;
            Map<String, Field> fieldMap = Cache.FIELD_CACHE.get(entity.getClass());
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
        if (Cache.FIELD_CACHE.containsKey(clazz)) {
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

        Cache.FIELD_CACHE.put(clazz, fieldMap);
    }

    /**
     * 从SQL中提取#{paramName}格式的参数名
     */
    private static List<String> extractNamedParamNames(String sql) {
        List<String> paramNames = new ArrayList<>();
        Matcher matcher = Cache.PARAM_PATTERN.matcher(sql);
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