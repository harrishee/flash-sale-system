package com.harris.controller.exception.handler;

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
    // 定义一个异常处理方法，处理所有的Exception和RuntimeException异常
    @ExceptionHandler({Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(); // 创建错误响应对象
        
        // 特殊处理UndeclaredThrowableException异常，检查其内部是否包装了FlowException
        if (e instanceof UndeclaredThrowableException) {
            // 设置错误代码和消息为限流错误
            errorResponse.setErrCode(ErrorCode.LIMIT_ERROR.getCode());
            errorResponse.setErrMessage(ErrorCode.LIMIT_ERROR.getMessage());
        } else {
            // 其他所有异常的处理，设置为内部服务器错误
            errorResponse.setErrCode(ErrorCode.INTERNAL_ERROR.getCode());
            errorResponse.setErrMessage(ErrorCode.INTERNAL_ERROR.getMessage());
        }
        
        log.error("InternalExceptionHandler: ", e);
        
        HttpHeaders httpHeaders = new HttpHeaders(); // 设置响应头，指定内容类型为JSON
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        // 返回构建的错误响应，状态码为 500 INTERNAL_SERVER_ERROR
        return handleExceptionInternal(e, JSON.toJSONString(errorResponse), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
