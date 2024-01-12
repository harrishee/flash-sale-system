package com.harris.infra.controller.exception;

public class AuthException extends RuntimeException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode.getErrDesc());
    }
}
