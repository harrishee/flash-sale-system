package com.harris.infra.controller.exception;

public class AuthException extends RuntimeException {
    public AuthException(AuthErrorCode authErrorCode) {
        super(authErrorCode.getErrDesc());
    }
}
