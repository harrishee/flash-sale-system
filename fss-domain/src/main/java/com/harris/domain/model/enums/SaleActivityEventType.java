package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum SaleActivityEventType {
    PUBLISHED(0),

    ONLINE(1),

    OFFLINE(2),

    MODIFIED(3);

    private final Integer code;

    SaleActivityEventType(Integer code) {
        this.code = code;
    }
}
