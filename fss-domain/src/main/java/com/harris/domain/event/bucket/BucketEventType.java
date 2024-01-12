package com.harris.domain.event.bucket;

public enum BucketEventType {
    DISABLED(0),
    ENABLED(1),
    ARRANGED(2);

    private final Integer code;

    BucketEventType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
