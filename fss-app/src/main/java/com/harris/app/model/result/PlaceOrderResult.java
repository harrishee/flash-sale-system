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

    // 同步下单
    public static PlaceOrderResult ok(Long orderId) {
        return new PlaceOrderResult().setSuccess(true).setOrderId(orderId).setMessage("抢购成功");
    }

    // 异步下单
    public static PlaceOrderResult ok(String placeOrderTaskId) {
        return new PlaceOrderResult().setSuccess(true).setPlaceOrderTaskId(placeOrderTaskId).setMessage("排队成功");
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
