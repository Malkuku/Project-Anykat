package com.anyview.xiazihao.sampleFlatMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class TypeConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private TypeConverter() {
        // 私有构造器防止实例化
    }

    /**
     * 判断是否为不可变类型
     */
    public static boolean isImmutableType(Class<?> clazz) {
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

    // 判断是否为简单类型
    public static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                Number.class.isAssignableFrom(type) ||
                type.equals(Boolean.class) ||
                type.equals(Character.class) ||
                type.equals(java.util.Date.class) ||
                type.equals(java.time.temporal.Temporal.class);
    }

    /**
     * 类型转换方法
     */
    public static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            if (targetType == Number.class) { // Number类型检查
                return 0;
            }
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }

        // 特殊处理JSON字符串到Map的转换
        if (value instanceof String && Map.class.isAssignableFrom(targetType)) {
            try {
                return objectMapper.readValue((String) value,
                        new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot convert JSON string to Map: " + value, e);
            }
        }

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

        // 字符串到其他类型的转换
        if (value instanceof String strValue) {
            try {
                if (targetType == Integer.class || targetType == int.class) {
                    return Integer.parseInt(strValue);
                }
                if (targetType == Long.class || targetType == long.class) {
                    return Long.parseLong(strValue);
                }
                if (targetType == Double.class || targetType == double.class) {
                    return Double.parseDouble(strValue);
                }
                if (targetType == Float.class || targetType == float.class) {
                    return Float.parseFloat(strValue);
                }
                if (targetType == Boolean.class || targetType == boolean.class) {
                    return Boolean.parseBoolean(strValue);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to convert string '" + strValue +
                        "' to type " + targetType.getName(), e);
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



        log.warn("Cannot convert value '{}' of type {} to target type {}",
                value, value.getClass().getName(), targetType.getName());
        throw new IllegalArgumentException("Cannot convert value '" + value +
                "' of type " + value.getClass().getName() +
                " to target type " + targetType.getName());
    }
}
