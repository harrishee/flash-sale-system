package com.harris.controller.interceptor;

import com.harris.app.service.app.AuthAppService;
import com.harris.app.model.auth.AuthResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * A simplified auth interceptor, converting token to user ID using Base64.
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String USER_ID = "userId";

    @Resource
    private AuthAppService authAppService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Return true if the user ID attribute already exists
        Object userId = request.getAttribute(USER_ID);
        if (userId != null) {
            return true;
        }

        // Otherwise, authenticate the token to get the result with user ID
        String token = request.getParameter("token");
        AuthResult authResult = authAppService.auth(token);

        // Wrap the original request and set the user ID attribute
        if (authResult.isSuccess()) {
            HttpServletRequestWrapper authRequestWrapper = new HttpServletRequestWrapper(request);
            authRequestWrapper.setAttribute(USER_ID, authResult.getUserId());
        }

        return true;
    }
}
