package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum BucketStatus {
    ENABLED(1),
    DISABLED(0);

    private final Integer code;

    BucketStatus(Integer code) {
        this.code = code;
    }
}
