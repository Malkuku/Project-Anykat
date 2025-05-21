package com.anyview.xiazihao.entity.exception;

// 验证失败异常
public class ValidationFailedException extends RuntimeException {
    public ValidationFailedException(String message) {
        super(message);
    }
}
