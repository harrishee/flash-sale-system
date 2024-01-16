package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderResult {
    private boolean success;
    private String code;
    private String msg;
    private String placeOrderTaskId;
    private Long orderId;

    public static PlaceOrderResult ok(Long orderId) {
        return new PlaceOrderResult().setSuccess(true).setOrderId(orderId);
    }

    public static PlaceOrderResult ok(String placeOrderTaskId) {
        return new PlaceOrderResult().setSuccess(true).setPlaceOrderTaskId(placeOrderTaskId);
    }

    public static PlaceOrderResult error(AppErrCode appErrorCode) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMsg(appErrorCode.getErrDesc());
    }

    public static PlaceOrderResult error(String code, String msg) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(code)
                .setMsg(msg);
    }
}
