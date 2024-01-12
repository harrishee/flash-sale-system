package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum FlashItemStatus {
    PUBLISHED(0),
    ONLINE(1),
    OFFLINE(-1);

    private final Integer code;

    FlashItemStatus(Integer code) {
        this.code = code;
    }

    public static boolean isOffline(Integer status) {
        return OFFLINE.getCode().equals(status);
    }

    public static boolean isOnline(Integer status) {
        return ONLINE.getCode().equals(status);
    }
}
