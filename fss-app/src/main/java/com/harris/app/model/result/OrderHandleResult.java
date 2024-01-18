package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import com.harris.app.model.enums.OrderTaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.harris.app.model.enums.OrderTaskStatus.SUCCESS;

@Data
@Accessors(chain = true)
public class OrderTaskHandleResult {
    private boolean success;
    private OrderTaskStatus orderTaskStatus;
    private Long orderId;
    private String code;
    private String msg;

    public static OrderTaskHandleResult ok(Long orderId) {
        return new OrderTaskHandleResult()
                .setSuccess(true)
                .setOrderTaskStatus(SUCCESS)
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
