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

    // Common
    SUCCESS(200, "SUCCESS"),

    ERROR(500, "Server Error"),

    // Login Module 5002xx
    LOGIN_ERROR(500210, "Invalid username or password"),

    FORMAT_ERROR(500211, "Invalid phone number format"),

    BIND_ERROR(500212, "Parameter validation failed"),

    USER_ID_NOT_EXIST(500213, "User ID does not exist"),

    SESSION_ERROR(500214, "Session does not exist or has expired"),

    // Flash Sale Module 5005xx
    EMPTY_STOCK(500500, "Out of stock"),

    REPEAT_ERROR(500501, "One item per person limit reached"),

    REQUEST_ILLEGAL(500502, "Illegal request, please try again"),

    ERROR_CAPTCHA(500503, "Incorrect captcha, please re-enter"),

    ACCESS_LIMIT_REACHED(500504, "Access rate limit exceeded, please try again later"),

    // Order Module 5003xx
    ORDER_NOT_EXIST(500300, "Order information does not exist"),

    ORDER_WRONG_STATUS(500301, "Invalid order status");

    private final Integer code;

    private final String message;
}
