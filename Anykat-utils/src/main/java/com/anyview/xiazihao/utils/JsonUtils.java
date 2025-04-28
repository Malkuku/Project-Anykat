package com.anyview.xiazihao.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper objectMapper;

    static {
        // 配置ObjectMapper实例
        objectMapper = new ObjectMapper();
        // 1. 忽略JSON中的未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 2. 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 3. 禁用日期作为时间戳的默认行为
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 4. 设置全局日期格式（传统Date和Java 8日期都适用）
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    }

    // 解析普通对象
    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Failed to parse JSON to " + clazz.getSimpleName(), e);
        }
    }

    // 解析集合
    public static <T> List<T> parseJsonToList(String json, Class<T> clazz) {
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Failed to parse JSON to List<" + clazz.getSimpleName() + ">", e);
        }
    }

    // 封装对象为JSON
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Failed to convert object to JSON", e);
        }
    }

    // 自定义异常
    public static class JsonParseException extends RuntimeException {
        public JsonParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}