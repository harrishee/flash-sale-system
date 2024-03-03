package com.harris.app.model;

import lombok.Getter;

@Getter
public enum PlaceOrderTaskStatus {
    SUBMITTED(0, "初始提交"),
    SUCCESS(1, "下单成功"),
    FAILED(-1, "下单失败");

    private final Integer status;
    private final String desc;

    PlaceOrderTaskStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static PlaceOrderTaskStatus getStatusByCode(Integer status) {
        if (status == null) {
            return null;
        }

        for (PlaceOrderTaskStatus placeOrderTaskStatus : PlaceOrderTaskStatus.values()) {
            if (placeOrderTaskStatus.getStatus().equals(status)) {
                return placeOrderTaskStatus;
            }
        }
        return null;
    }
}
