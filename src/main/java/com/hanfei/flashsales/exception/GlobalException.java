package com.hanfei.flashsales.exception;

import com.hanfei.flashsales.vo.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局异常类，用于封装自定义异常
 *
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {

    private ResultEnum resultEnum;
}
