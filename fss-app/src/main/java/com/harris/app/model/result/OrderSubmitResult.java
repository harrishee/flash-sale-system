package com.harris.app.model.result;

import com.harris.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderSubmitResult {
    private boolean success;
    private String code;
    private String message;
    
    public static OrderSubmitResult ok() {
        return new OrderSubmitResult().setSuccess(true);
    }
    
    public static OrderSubmitResult error(AppErrorCode appErrorCode) {
        return new OrderSubmitResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }
}
