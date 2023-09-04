package com.hanfei.flashsales.exception;

import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Responsible for handling exceptions globally within the application
 *
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle all exceptions and return a common error response
     */
    @ExceptionHandler(Exception.class)
    public Result ExceptionHandler(Exception e) {
        if (e instanceof GlobalException) {
            GlobalException exception = (GlobalException) e;
            return Result.error(exception.getResultEnum());
        } else if (e instanceof BindException) {
            BindException bindException = (BindException) e;
            Result result = Result.error(ResultEnum.BIND_ERROR);
            result.setMessage("Parameter validation exception: "
                    + bindException
                    .getBindingResult()
                    .getAllErrors()
                    .get(0)
                    .getDefaultMessage()
            );
            return result;
        }
        log.error("Unknown exception: {}", e.toString());
        return Result.error(ResultEnum.ERROR);
    }
}
