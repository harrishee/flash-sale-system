package com.harris.domain.exception;

import com.alibaba.cola.dto.ErrorCodeI;

public enum DomainErrorCode implements ErrorCodeI {
    INVALID_PARAMS("INVALID_PARAMS", "参数错误"),
    ACTIVITY_NOT_EXIST("ACTIVITY_NOT_EXIST", "活动不存在"),
    ACTIVITY_NOT_ONLINE("ACTIVITY_NOT_ONLINE", "活动尚未上线"),
    ITEM_NOT_EXIST("ITEM_NOT_EXIST", "商品不存在"),
    ITEM_NOT_ONLINE("ITEM_NOT_ONLINE", "商品尚未上线"),
    ORDER_NOT_EXIST("ORDER_NOT_EXIST", "订单不存在");
    
    private final String errCode;
    private final String errDesc;
    
    DomainErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }
    
    @Override
    public String getErrCode() {
        return errCode;
    }
    
    @Override
    public String getErrDesc() {
        return errDesc;
    }
}
