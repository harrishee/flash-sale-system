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

    public static PlaceOrderResult error(AppErrCode appErrorCode) {
        return new PlaceOrderResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMsg(appErrorCode.getErrDesc());
    }
}
