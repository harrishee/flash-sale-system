package com.harris.app.exception;

import com.alibaba.cola.dto.ErrorCodeI;

public enum AppErrorCode implements ErrorCodeI {
    INVALID_PARAMS("INVALID_PARAMS", "应用层，参数错误"),
    TRY_LATER("TRY_LATER", "应用层，稍后再试"),
    LOCK_FAILED("LOCK_FAILED_ERROR", "分布式锁失败，稍后再试"),
    ACTIVITY_PUBLISH_FAILED("ACTIVITY_PUBLISH_FAILED", "活动发布失败"),
    ACTIVITY_MODIFY_FAILED("ACTIVITY_MODIFY_FAILED", "活动修改失败"),
    ACTIVITY_NOT_FOUND("ACTIVITY_NOT_FOUND", "活动不存在"),
    ITEM_PUBLISH_FAILED("ITEM_PUBLISH_FAILED", "抢购品发布失败"),
    ITEM_NOT_FOUND("ITEM_NOT_FOUND", "抢购品不存在"),
    ITEM_NOT_ON_SALE("ITEM_NOT_ON_SALE", "当前不是抢购时段"),
    GET_ITEM_FAILED("GET_ITEM_FAILED", "获取抢购品失败"),
    ORDER_TOKENS_NOT_AVAILABLE("ORDER_TOKENS_NOT_AVAILABLE", "暂无可用库存"),
    ORDER_TASK_SUBMIT_FAILED("ORDER_TASK_SUBMIT_FAILED", "订单提交失败，请稍后再试"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "订单不存在"),
    ORDER_TYPE_NOT_SUPPORT("ORDER_TYPE_NOT_SUPPORT", "下单类型不支持"),
    ORDER_CANCEL_FAILED("ORDER_CANCEL_FAILED", "订单取消失败"),
    PLACE_ORDER_FAILED("PLACE_ORDER_FAILED", "下单失败"),
    PLACE_ORDER_TASK_ID_INVALID("PLACE_ORDER_TASK_ID_INVALID", "下单任务编号错误"),
    REDUNDANT_SUBMIT("REDUNDANT_SUBMIT", "重复提交");

    private final String errCode;
    private final String errDesc;

    private AppErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    @Override
    public String getErrCode() {
        return errCode;
    }

    @Override
    public String getErrDesc() {
        return errDesc;
    }
}
