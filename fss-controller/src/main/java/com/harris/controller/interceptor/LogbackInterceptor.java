package com.harris.controller.interceptor;

import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class LogbackInterceptor extends HandlerInterceptorAdapter {
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Generate a unique trace ID and add it to MDC for logging
        MDC.put(MDC_TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Remove the trace ID from MDC after request completion
        MDC.remove(MDC_TRACE_ID);
        super.afterCompletion(request, response, handler, ex);
    }
}
