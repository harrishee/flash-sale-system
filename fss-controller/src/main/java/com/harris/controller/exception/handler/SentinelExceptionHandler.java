package com.harris.controller.exception.handler;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.UndeclaredThrowableException;

import static com.harris.controller.exception.handler.ErrCode.DEGRADE_BLOCK;
import static com.harris.controller.exception.handler.ErrCode.LIMIT_ERROR;

@Slf4j
@ControllerAdvice
public class SentinelExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UndeclaredThrowableException.class)
    protected ResponseEntity<Object> handleConflict(UndeclaredThrowableException e, WebRequest request) {
        ErrResponse errResponse = new ErrResponse();
        if (e.getUndeclaredThrowable() instanceof FlowException) {
            // Handling for FlowException
            errResponse.setErrCode(LIMIT_ERROR.getCode());
            errResponse.setErrCode(LIMIT_ERROR.getMsg());
        }
        if (e.getUndeclaredThrowable() instanceof DegradeException) {
            // Handling for DegradeException
            errResponse.setErrCode(DEGRADE_BLOCK.getCode());
            errResponse.setErrCode(DEGRADE_BLOCK.getMsg());
        }
        log.error("SentinelExceptionHandler: ", e);

        // Creating HttpHeaders, setting content type to JSON
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // Returning error response with 400 BAD_REQUEST status
        return handleExceptionInternal(e, JSON.toJSONString(errResponse),
                httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}
