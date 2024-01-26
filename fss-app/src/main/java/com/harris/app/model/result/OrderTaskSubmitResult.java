package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderTaskSubmitResult {
    private boolean success;
    private String code;
    private String msg;

    public static OrderTaskSubmitResult ok() {
        return new OrderTaskSubmitResult().setSuccess(true);
    }

    public static OrderTaskSubmitResult error(AppErrCode appErrCode) {
        return new OrderTaskSubmitResult()
                .setSuccess(false)
                .setCode(appErrCode.getErrCode())
                .setMsg(appErrCode.getErrDesc());
    }
}
