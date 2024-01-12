package com.harris.domain.model.enums;

import lombok.Getter;

@Getter
public enum BucketType {
    PRIMARY(0);

    private final Integer code;

    BucketType(Integer code) {
        this.code = code;
    }
}
