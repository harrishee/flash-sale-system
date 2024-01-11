package com.harris.infra.controller.exception;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public class AuthException extends RuntimeException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode.getErrDesc());
    }
}
