package com.hanfei.flashsales.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Getter
@ToString
@AllArgsConstructor
public enum ResultEnum {

    // 通用
    SUCCESS(200, "SUCCESS"),

    ERROR(500, "服务端异常"),

    // 登录模块 5002xx
    LOGIN_ERROR(500210, "用户名或密码不正确"),

    FORMAT_ERROR(500211, "手机号码格式不正确"),

    BIND_ERROR(500212, "参数校验异常"),

    USER_ID_NOT_EXIST(500213, "用户ID不存在"),

    SESSION_ERROR(500214, "session 不存在或者已经失效"),

    // 秒杀模块 5005xx
    EMPTY_STOCK(500500, "库存不足"),

    REPEAT_ERROR(500501, "该商品每人限购一件"),

    REQUEST_ILLEGAL(500502, "请求非法，请重新尝试"),

    ERROR_CAPTCHA(500503, "验证码错误，请重新输入"),

    ACCESS_LIMIT_REAHCED(500504, "访问过于频繁，请稍后再试"),

    // 订单模块 5003xx
    ORDER_NOT_EXIST(500300, "订单信息不存在"),

    ORDER_WRONG_STATUS(500300, "订单状态无效");

    private final Integer code;

    private final String message;
}
