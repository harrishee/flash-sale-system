package com.harris.app.model.result;

import com.harris.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderResult {
    private String placeOrderTaskId;
    private Long orderId;
    private boolean success;
    private String code;
    private String message;

    public static PlaceOrderResult ok(Long orderId) {
        return new PlaceOrderResult().setSuccess(true).setOrderId(orderId);
    }

    public static PlaceOrderResult ok(String placeOrderTaskId) {
        return new PlaceOrderResult().setSuccess(true).setPlaceOrderTaskId(placeOrderTaskId);
    }

    public static PlaceOrderResult error(AppErrorCode appErrorCode) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }

    public static PlaceOrderResult error(String code, String message) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(code)
                .setMessage(message);
    }
}
