package com.harris.controller.interceptor;

import com.harris.app.service.app.AuthService;
import com.harris.app.model.auth.AuthResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String USER_ID = "userId"; // 用户ID的请求属性键
    
    @Resource
    private AuthService authService; // 注入认证服务
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果请求中已存在用户ID属性，则直接通过
        Object userId = request.getAttribute(USER_ID);
        if (userId != null) return true;
        
        // 否则，从请求中获取token并进行认证，获取认证结果
        String token = request.getParameter("token");
        AuthResult authResult = authService.auth(token);
        
        // 如果认证成功，包装原始请求对象，并设置用户ID属性
        if (authResult.isSuccess()) {
            HttpServletRequestWrapper authRequestWrapper = new HttpServletRequestWrapper(request);
            authRequestWrapper.setAttribute(USER_ID, authResult.getUserId());
        }
        
        return true; // 继续执行后续的处理流程
    }
}
