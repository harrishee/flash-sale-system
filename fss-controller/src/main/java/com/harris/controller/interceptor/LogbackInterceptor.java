package com.harris.controller.interceptor;

import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class LogbackInterceptor extends HandlerInterceptorAdapter {
    private static final String MDC_TRACE_ID = "traceId"; // MDC中追踪ID的键
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成一个唯一的追踪ID，并添加到MDC中，用于日志记录
        MDC.put(MDC_TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        return super.preHandle(request, response, handler);
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后，从MDC中移除追踪ID
        MDC.remove(MDC_TRACE_ID);
        super.afterCompletion(request, response, handler, ex);
    }
}
