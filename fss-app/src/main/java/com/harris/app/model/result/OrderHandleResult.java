package com.harris.app.model.result;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.model.PlaceOrderTaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderHandleResult {
    private boolean success;
    private String code;
    private String message;
    private PlaceOrderTaskStatus placeOrderTaskStatus;
    private Long orderId;
    
    public static OrderHandleResult ok(Long orderId) {
        return new OrderHandleResult()
                .setSuccess(true)
                .setPlaceOrderTaskStatus(PlaceOrderTaskStatus.SUCCESS)
                .setOrderId(orderId);
    }
    
    public static OrderHandleResult error(AppErrorCode appErrorCode) {
        return new OrderHandleResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }
    
    public static OrderHandleResult error(PlaceOrderTaskStatus placeOrderTaskStatus) {
        return new OrderHandleResult()
                .setSuccess(false)
                .setPlaceOrderTaskStatus(placeOrderTaskStatus);
    }
}
