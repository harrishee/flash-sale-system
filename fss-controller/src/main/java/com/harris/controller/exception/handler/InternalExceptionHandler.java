package com.harris.controller.exception.handler;

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

import static com.harris.controller.model.ExceptionCode.INTERNAL_ERROR;
import static com.harris.controller.model.ExceptionCode.LIMIT_BLOCK;

@Slf4j
@ControllerAdvice
public class InternalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        if (e instanceof UndeclaredThrowableException) {
            if (((UndeclaredThrowableException) e).getUndeclaredThrowable() instanceof FlowException) {
                exceptionResponse.setErrCode(LIMIT_BLOCK.getCode());
                exceptionResponse.setErrMsg(LIMIT_BLOCK.getDesc());
            }
        } else {
            exceptionResponse.setErrCode(INTERNAL_ERROR.getCode());
            exceptionResponse.setErrMsg(INTERNAL_ERROR.getDesc());
        }

        log.error("InternalExceptionHandler: {}", e.getClass());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(e, JSON.toJSONString(exceptionResponse), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
