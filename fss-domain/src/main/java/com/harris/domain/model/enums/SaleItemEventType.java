package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum SaleItemEventType {
    PUBLISHED(0),
    ONLINE(1),
    OFFLINE(2);

    private final Integer code;

    SaleItemEventType(Integer code) {
        this.code = code;
    }

}
