package com.harris.infra.controller.exception;

import com.alibaba.cola.dto.ErrorCodeI;
import lombok.Getter;

@Getter
public enum ErrorCode implements ErrorCodeI {
    INVALID_TOKEN("INVALID_TOKEN", "无效token"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "访问未授权");

    private final String errCode;
    private final String errDesc;

    ErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }
}
