package com.harris.app.model.enums;

import lombok.Getter;

@Getter
public enum ArrangeMode {
    TOTAL(1),
    INCREMENTAL(2);

    private final Integer mode;

    ArrangeMode(Integer mode) {
        this.mode = mode;
    }

    public static boolean isTotalAmountMode(Integer mode) {
        return TOTAL.mode.equals(mode);
    }

    public static boolean isIncrementalAmountMode(Integer mode) {
        return INCREMENTAL.mode.equals(mode);
    }
}
