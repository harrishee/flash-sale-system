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

@Slf4j
@ControllerAdvice
public class InternalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        if (e instanceof UndeclaredThrowableException) {
            // Handling for UndeclaredThrowableException wrapping a FlowException
            if (((UndeclaredThrowableException) e).getUndeclaredThrowable() instanceof FlowException) {
                errorResponse.setErrCode(ErrorCode.LIMIT_ERROR.getCode());
                errorResponse.setErrMessage(ErrorCode.LIMIT_ERROR.getMessage());
            }
        } else {
            // Handling for other exceptions
            errorResponse.setErrCode(ErrorCode.INTERNAL_ERROR.getCode());
            errorResponse.setErrMessage(ErrorCode.INTERNAL_ERROR.getMessage());
        }

        log.error("InternalExceptionHandler: ", e);

        // Creating HttpHeaders, setting content type to JSON
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Returning error response with 500 INTERNAL_SERVER_ERROR status
        return handleExceptionInternal(e, JSON.toJSONString(errorResponse),
                httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
