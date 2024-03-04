package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum SaleOrderEventType {
    CREATED(0),
    CANCEL(1);
    
    private final Integer code;
    
    SaleOrderEventType(Integer code) {
        this.code = code;
    }
}
