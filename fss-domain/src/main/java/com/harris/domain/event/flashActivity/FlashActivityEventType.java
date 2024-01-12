package com.harris.domain.event.flashActivity;

import lombok.Getter;

@Getter
public enum FlashActivityEventType {
    PUBLISHED(0),

    ONLINE(1),

    OFFLINE(2),

    MODIFIED(3);

    private final Integer code;

    FlashActivityEventType(Integer code) {
        this.code = code;
    }
}
