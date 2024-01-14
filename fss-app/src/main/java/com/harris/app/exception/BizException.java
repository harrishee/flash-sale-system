package com.harris.app.exception;

public class BizException extends AppException{
    public BizException(AppErrCode errCode) {
        super(errCode.getErrDesc());
    }

    public BizException(String message) {
        super(message);
    }
}
