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

@Slf4j
@ControllerAdvice
public class SentinelExceptionHandler extends ResponseEntityExceptionHandler {
    // 处理由Sentinel引起的UndeclaredThrowableException异常
    @ExceptionHandler(UndeclaredThrowableException.class)
    protected ResponseEntity<Object> handleConflict(UndeclaredThrowableException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(); // 创建错误响应对象
        
        // 检查UndeclaredThrowableException内部是否包装了FlowException或DegradeException
        if (e.getUndeclaredThrowable() instanceof FlowException) {
            // 设置错误代码和消息为限流错误
            errorResponse.setErrCode(ErrorCode.LIMIT_ERROR.getCode());
            errorResponse.setErrMessage(ErrorCode.LIMIT_ERROR.getMessage());
        }
        if (e.getUndeclaredThrowable() instanceof DegradeException) {
            // 设置错误代码和消息为降级错误
            errorResponse.setErrCode(ErrorCode.DEGRADE_BLOCK.getCode());
            errorResponse.setErrMessage(ErrorCode.DEGRADE_BLOCK.getMessage());
        }
        
        log.error("SentinelExceptionHandler: ", e); // 记录错误日志
        
        HttpHeaders httpHeaders = new HttpHeaders(); // 设置响应头，指定内容类型为JSON
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        // 返回构建的错误响应，状态码为 400 BAD_REQUEST
        return handleExceptionInternal(e, JSON.toJSONString(errorResponse), httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}
