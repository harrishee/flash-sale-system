package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum SaleOrderStatus {
    CREATED(1),
    PAID(2),
    CANCELED(0),
    DELETED(-1);
    
    private final Integer code;
    
    SaleOrderStatus(Integer code) {
        this.code = code;
    }
    
    public static boolean isCanceled(Integer status) {
        return CANCELED.getCode().equals(status);
    }
}
