package com.anyview.xiazihao.entity.exception;

//  缺少参数异常
public class IncompleteParameterException extends RuntimeException {
    public IncompleteParameterException(String message) {
        super(message);
    }
}
