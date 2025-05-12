package com.anyview.xiazihao.entity.exception;

//没有在数据库找到内容
public class NoDatabaseContentException extends RuntimeException {
    public NoDatabaseContentException(String message) {
        super(message);
    }

    public NoDatabaseContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
