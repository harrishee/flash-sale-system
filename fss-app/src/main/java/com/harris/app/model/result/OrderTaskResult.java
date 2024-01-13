package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import com.harris.app.model.enums.OrderTaskStatus;
import lombok.Data;

import static com.harris.app.model.enums.OrderTaskStatus.SUCCESS;

@Data
public class OrderTaskResult {
    private boolean success;
    private OrderTaskStatus orderTaskStatus;
    private Long orderId;
    private String code;
    private String msg;

    public static OrderTaskResult ok(Long orderId) {
        OrderTaskResult orderTaskResult = new OrderTaskResult();
        orderTaskResult.setSuccess(true);
        orderTaskResult.setOrderTaskStatus(SUCCESS);
        orderTaskResult.setOrderId(orderId);
        return orderTaskResult;
    }

    public static OrderTaskResult error(AppErrCode appErrCode) {
        OrderTaskResult orderTaskResult = new OrderTaskResult();
        orderTaskResult.setSuccess(false);
        orderTaskResult.setCode(appErrCode.getErrCode());
        orderTaskResult.setMsg(appErrCode.getErrDesc());
        return orderTaskResult;
    }

    public static OrderTaskResult error(OrderTaskStatus orderTaskStatus) {
        OrderTaskResult orderTaskResult = new OrderTaskResult();
        orderTaskResult.setSuccess(false);
        orderTaskResult.setOrderTaskStatus(orderTaskStatus);
        return orderTaskResult;
    }
}
