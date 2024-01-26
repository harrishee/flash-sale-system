package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PurchaseResult {
    private boolean success;
    private String code;
    private String msg;
    private String placeOrderTaskId;
    private Long orderId;

    public static PurchaseResult ok(Long orderId) {
        return new PurchaseResult().setSuccess(true).setOrderId(orderId);
    }

    public static PurchaseResult ok(String placeOrderTaskId) {
        return new PurchaseResult().setSuccess(true).setPlaceOrderTaskId(placeOrderTaskId);
    }

    public static PurchaseResult error(AppErrCode appErrorCode) {
        return new PurchaseResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMsg(appErrorCode.getErrDesc());
    }

    public static PurchaseResult error(String code, String msg) {
        return new PurchaseResult()
                .setSuccess(false)
                .setCode(code)
                .setMsg(msg);
    }
}
