package com.harris.controller.exception.handler;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson.JSON;
import com.harris.app.exception.BizException;
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

import static com.harris.controller.exception.handler.ErrCode.*;

@Slf4j
@ControllerAdvice
public class BadRequestHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({FlowException.class, AuthException.class, BizException.class, DomainException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException e, WebRequest request) {
        ErrResponse errResponse = new ErrResponse();

        if (e instanceof UndeclaredThrowableException) {
            // Handling for UndeclaredThrowableException wrapping a FlowException
            if (((UndeclaredThrowableException) e).getUndeclaredThrowable() instanceof FlowException) {
                errResponse.setErrCode(LIMIT_ERROR.getCode());
                errResponse.setErrMsg(LIMIT_ERROR.getMsg());
            }
        } else if (e instanceof BizException || e instanceof DomainException) {
            // Handling for business and domain exceptions
            errResponse.setErrCode(BIZ_ERROR.getCode());
            errResponse.setErrMsg(e.getMessage());
        } else if (e instanceof AuthException) {
            // Handling for authentication exceptions
            errResponse.setErrCode(AUTH_ERROR.getCode());
            errResponse.setErrMsg(AUTH_ERROR.getMsg());
        }
        log.error("BadRequestHandler: ", e);

        // Creating HttpHeaders, setting content type to JSON
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // Returning error response with 400 BAD_REQUEST status
        return handleExceptionInternal(e, JSON.toJSONString(errResponse),
                httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}
