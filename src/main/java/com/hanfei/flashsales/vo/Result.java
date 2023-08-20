package com.hanfei.flashsales.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private long code;

    private String message;

    private Object object;

    public static Result success() {
        return new Result(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), null);
    }

    public static Result success(Object object) {
        return new Result(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), object);
    }

    public static Result error(ResultEnum resultEnum) {
        return new Result(resultEnum.getCode(), resultEnum.getMessage(), null);
    }

    public static Result error(ResultEnum resultEnum, Object obj) {
        return new Result(resultEnum.getCode(), resultEnum.getMessage(), obj);
    }
}
