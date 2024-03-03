package com.harris.infra.config;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Aspect for managing log trace IDs.
 * It sets a unique trace ID for each method annotated with @MarkTrace at
 * the beginning and removes it after the method returns.
 */
@Aspect
@Component
public class MarkTraceAspect {
    private static final String MDC_TRACE_ID = "traceId";
    
    @Pointcut("@annotation(com.harris.infra.config.MarkTrace)")
    public void traceMethod() {
        // 定义切点，针对使用@MarkTrace注解的方法
    }
    
    @Before("traceMethod()")
    public void before() {
        // 方法执行前，向MDC中添加唯一的traceId
        if (MDC.get(MDC_TRACE_ID) == null) {
            MDC.put(MDC_TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        }
    }
    
    @AfterReturning(pointcut = "traceMethod()")
    public void after() {
        // 方法执行后，从MDC中移除traceId
        MDC.remove(MDC_TRACE_ID);
    }
}
