package com.harris.controller.exception.handler;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson.JSON;
import com.harris.controller.model.response.ExceptionResponse;
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

import static com.harris.controller.model.ExceptionCode.DEGRADE_BLOCK;
import static com.harris.controller.model.ExceptionCode.LIMIT_BLOCK;

@Slf4j
@ControllerAdvice
public class SentinelExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {UndeclaredThrowableException.class})
    protected ResponseEntity<Object> handleConflict(UndeclaredThrowableException e, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        if (e.getUndeclaredThrowable() instanceof FlowException) {
            exceptionResponse.setErrCode(LIMIT_BLOCK.getCode());
            exceptionResponse.setErrCode(LIMIT_BLOCK.getDesc());
        }
        if (e.getUndeclaredThrowable() instanceof DegradeException) {
            exceptionResponse.setErrCode(DEGRADE_BLOCK.getCode());
            exceptionResponse.setErrCode(DEGRADE_BLOCK.getDesc());
        }
        log.error("SentinelExceptionHandler: {}", e.getMessage(), e);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(e, JSON.toJSONString(exceptionResponse), httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}
