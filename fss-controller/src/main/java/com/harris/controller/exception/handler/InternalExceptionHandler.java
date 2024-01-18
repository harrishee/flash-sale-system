package com.harris.controller.exception.handler;

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

import static com.harris.controller.exception.handler.ErrCode.INTERNAL_ERROR;
import static com.harris.controller.exception.handler.ErrCode.LIMIT_ERROR;

@Slf4j
@ControllerAdvice
public class InternalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(Exception e, WebRequest request) {
        ErrResponse errResponse = new ErrResponse();
        if (e instanceof UndeclaredThrowableException) {
            // Handling for UndeclaredThrowableException wrapping a FlowException
            if (((UndeclaredThrowableException) e).getUndeclaredThrowable() instanceof FlowException) {
                errResponse.setErrCode(LIMIT_ERROR.getCode());
                errResponse.setErrMsg(LIMIT_ERROR.getMsg());
            }
        } else {
            // Handling for other exceptions
            errResponse.setErrCode(INTERNAL_ERROR.getCode());
            errResponse.setErrMsg(INTERNAL_ERROR.getMsg());
        }
        log.error("InternalExceptionHandler: ", e);

        // Creating HttpHeaders, setting content type to JSON
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // Returning error response with 500 INTERNAL_SERVER_ERROR status
        return handleExceptionInternal(e, JSON.toJSONString(errResponse),
                httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
