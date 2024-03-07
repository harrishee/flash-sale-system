package com.harris.controller.exception.handler;

import lombok.Getter;

@Getter
public enum ErrorCode {
    LIMIT_ERROR("01", "操作频繁，请稍后再试"),
    INTERNAL_ERROR("02", "服务器内部错误"),
    BIZ_ERROR("03", "客户端参数或操作错误"),
    AUTH_ERROR("04", "鉴权错误");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
