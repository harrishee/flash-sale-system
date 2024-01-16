package com.harris.app.model.enums;

import lombok.Getter;

@Getter
public enum OrderTaskStatus {
    SUBMITTED(0, "初始提交"),
    SUCCESS(1, "下单成功"),
    FAILED(-1, "下单失败");

    private final Integer status;
    private final String desc;

    OrderTaskStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
