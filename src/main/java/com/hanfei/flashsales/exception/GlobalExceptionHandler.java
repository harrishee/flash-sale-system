package com.hanfei.flashsales.exception;

import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类，用于统一处理异常情况并返回合适的响应
 *
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有异常，返回通用的错误响应
     *
     * @param ex 异常对象
     * @return 通用错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result ExceptionHandler(Exception ex) {
        if (ex instanceof GlobalException) {
            // 如果是自定义的 GlobalException 异常
            GlobalException exception = (GlobalException) ex;
            return Result.error(exception.getResultEnum());

        } else if (ex instanceof BindException) {
            // 如果是参数校验异常（BindException）
            BindException bindException = (BindException) ex;
            Result result = Result.error(ResultEnum.BIND_ERROR);

            // 获取校验错误信息并封装为响应消息
            result.setMessage("参数校验异常：" + bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return result;
        }
        log.error("***GlobalExceptionHandler*** 未知异常: {}", ex.toString());
        return Result.error(ResultEnum.ERROR);
    }
}
