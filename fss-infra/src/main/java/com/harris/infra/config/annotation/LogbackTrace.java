package com.harris.infra.config.annotation;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class LogbackTrace {
    private static final String TRACE_ID = "traceId";

    @Pointcut("@annotation(com.harris.infra.config.annotation.MarkTrace)")
    public void traceMethod() {
    }

    @Before("traceMethod()")
    public void before() {
        if (MDC.get(TRACE_ID) == null) {
            MDC.put(TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        }
    }

    @AfterReturning(pointcut = "traceMethod()")
    public void after() {
        MDC.remove(TRACE_ID);
    }
}
