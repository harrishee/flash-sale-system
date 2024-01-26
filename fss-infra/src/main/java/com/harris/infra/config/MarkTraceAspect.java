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

    /**
     * Pointcut that matches methods annotated with @MarkTrace.
     * It is used to define where advice should be applied.
     */
    @Pointcut("@annotation(com.harris.infra.config.MarkTrace)")
    public void traceMethod() {
    }

    /**
     * Before advice that runs before methods matched by the traceMethod pointcut.
     * It checks if a trace ID is not already present in MDC, and if not,
     * it generates a new trace ID and adds it to the MDC.
     */
    @Before("traceMethod()")
    public void before() {
        if (MDC.get(MDC_TRACE_ID) == null) {
            MDC.put(MDC_TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        }
    }

    /**
     * After returning advice that runs after methods matched by the traceMethod pointcut.
     * It removes the trace ID from the MDC, ensuring it's not used for subsequent unrelated log entries.
     */
    @AfterReturning(pointcut = "traceMethod()")
    public void after() {
        MDC.remove(MDC_TRACE_ID);
    }
}
