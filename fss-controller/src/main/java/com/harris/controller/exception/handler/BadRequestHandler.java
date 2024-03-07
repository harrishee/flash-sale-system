package com.harris.controller.exception.handler;

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

@Slf4j
@ControllerAdvice
public class BadRequestHandler extends ResponseEntityExceptionHandler {
    // 定义一个异常处理方法，处理一系列自定义异常
    @ExceptionHandler({AuthException.class, BizException.class, DomainException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(); // 创建错误响应对象
        
        // 特殊处理UndeclaredThrowableException异常，检查其内部是否包装了FlowException
        if (e instanceof UndeclaredThrowableException) {
            // 设置错误代码和消息为限流错误
            errorResponse.setErrCode(ErrorCode.LIMIT_ERROR.getCode());
            errorResponse.setErrMessage(ErrorCode.LIMIT_ERROR.getMessage());
        } else if (e instanceof BizException || e instanceof DomainException) {
            // 业务异常和领域异常的处理
            errorResponse.setErrCode(ErrorCode.BIZ_ERROR.getCode());
            errorResponse.setErrMessage(e.getMessage());
        } else if (e instanceof AuthException) {
            // 认证异常的处理
            errorResponse.setErrCode(ErrorCode.AUTH_ERROR.getCode());
            errorResponse.setErrMessage(ErrorCode.AUTH_ERROR.getMessage());
        }
        
        log.error("BadRequestHandler: ", e);
        
        HttpHeaders httpHeaders = new HttpHeaders(); // 设置响应头，指定内容类型为JSON
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        // 返回构建的错误响应，状态码为 400 BAD_REQUEST
        return handleExceptionInternal(e, JSON.toJSONString(errorResponse), httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}
