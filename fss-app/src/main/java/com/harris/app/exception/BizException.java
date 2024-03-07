package com.harris.app.exception;

public class BizException extends RuntimeException{
    public BizException(AppErrorCode appErrorCode) {
        super(appErrorCode.getErrDesc());
    }

    public BizException(String message) {
        super(message);
    }
}
