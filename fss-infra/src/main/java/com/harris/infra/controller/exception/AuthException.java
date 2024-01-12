package com.harris.infra.controller.exception;

public class AuthException extends RuntimeException {
    public AuthException(AuthErrCode authErrCode) {
        super(authErrCode.getErrDesc());
    }
}
