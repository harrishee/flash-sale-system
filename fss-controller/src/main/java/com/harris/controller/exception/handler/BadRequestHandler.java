package com.harris.controller.exception.handler;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson.JSON;
import com.harris.app.exception.BizException;
import com.harris.controller.model.response.ExceptionResponse;
import com.harris.domain.exception.DomainException;
import com.harris.infra.controller.exception.AuthException;
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

import static com.harris.controller.model.ExceptionCode.*;
import static com.harris.controller.model.ExceptionCode.AUTH_ERROR;

@Slf4j
@ControllerAdvice
public class BadRequestHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {BizException.class, FlowException.class, AuthException.class, DomainException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException e, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        if (e instanceof UndeclaredThrowableException) {
            if (((UndeclaredThrowableException) e).getUndeclaredThrowable() instanceof FlowException) {
                exceptionResponse.setErrCode(LIMIT_BLOCK.getCode());
                exceptionResponse.setErrMsg(LIMIT_BLOCK.getDesc());
            }
        } else if (e instanceof BizException || e instanceof DomainException) {
            exceptionResponse.setErrCode(BIZ_ERROR.getCode());
            exceptionResponse.setErrMsg(e.getMessage());
        } else if (e instanceof AuthException) {
            exceptionResponse.setErrCode(AUTH_ERROR.getCode());
            exceptionResponse.setErrMsg(AUTH_ERROR.getDesc());
        }

        log.error("BadRequestHandler: {}", e.getMessage(), e);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(e, JSON.toJSONString(exceptionResponse), httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}
