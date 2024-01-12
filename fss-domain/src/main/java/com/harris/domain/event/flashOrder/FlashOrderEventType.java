package com.harris.domain.event.flashOrder;

public enum FlashOrderEventType {
    CREATED(0),
    CANCEL(1);

    private final Integer code;

    FlashOrderEventType(Integer code) {
        this.code = code;
    }
}
