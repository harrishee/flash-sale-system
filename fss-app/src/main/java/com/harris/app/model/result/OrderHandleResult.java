package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import com.harris.app.model.enums.OrderTaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.harris.app.model.enums.OrderTaskStatus.SUCCESS;

@Data
@Accessors(chain = true)
public class OrderHandleResult {
    private boolean success;
    private OrderTaskStatus orderTaskStatus;
    private Long orderId;
    private String code;
    private String msg;

    public static OrderHandleResult ok(Long orderId) {
        return new OrderHandleResult()
                .setSuccess(true)
                .setOrderTaskStatus(SUCCESS)
                .setOrderId(orderId);
    }

    public static OrderHandleResult error(AppErrCode appErrCode) {
        return new OrderHandleResult()
                .setSuccess(false)
                .setCode(appErrCode.getErrCode())
                .setMsg(appErrCode.getErrDesc());
    }

    public static OrderHandleResult error(OrderTaskStatus orderTaskStatus) {
        return new OrderHandleResult()
                .setSuccess(false)
                .setOrderTaskStatus(orderTaskStatus);
    }
}
