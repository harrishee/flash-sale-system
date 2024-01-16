package com.harris.app.exception;

public class BizException extends AppException{
    public BizException(AppErrCode appErrCode) {
        super(appErrCode.getErrDesc());
    }

    public BizException(String msg) {
        super(msg);
    }
}
