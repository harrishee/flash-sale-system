package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import com.harris.app.model.enums.OrderTaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderTaskSubmitResult {
    private boolean success;
    private OrderTaskStatus orderTaskStatus;
    private Long orderId;
    private String code;
    private String message;

    public static OrderTaskHandleResult ok(Long orderId) {
        return new OrderTaskHandleResult()
                .setSuccess(true)
                .setOrderTaskStatus(OrderTaskStatus.SUCCESS)
                .setOrderId(orderId);
    }

    public static OrderTaskHandleResult error(AppErrCode appErrCode) {
        return new OrderTaskHandleResult()
                .setSuccess(false)
                .setCode(appErrCode.getErrCode())
                .setMsg(appErrCode.getErrDesc());
    }

    public static OrderTaskHandleResult error(OrderTaskStatus orderTaskStatus) {
        return new OrderTaskHandleResult()
                .setSuccess(false)
                .setOrderTaskStatus(orderTaskStatus);
    }
}
