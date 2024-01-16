package com.harris.app.exception;

public class PlaceOrderException extends RuntimeException {
    public PlaceOrderException(AppErrCode appErrCode) {
        super(appErrCode.getErrDesc());
    }
}
