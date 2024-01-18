package com.harris.controller.interceptor;

import com.harris.app.auth.AuthAppService;
import com.harris.app.auth.model.AuthResult;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * A simplified auth interceptor for adding userId to the request.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String USER_ID = "userId";

    @Resource
    private AuthAppService authAppService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Return true if userId is not null
        Object userId = request.getAttribute(USER_ID);
        if (userId != null) {
            return true;
        }

        // Otherwise, get token from request and authenticate
        String token = request.getParameter("token");
        AuthResult authResult = authAppService.auth(token);
        if (authResult.isSuccess()) {
            // Wrap the original request and set the user ID attribute
            HttpServletRequestWrapper authRequestWrapper = new HttpServletRequestWrapper(request);
            authRequestWrapper.setAttribute(USER_ID, authResult.getUserId());
        }
        return true;
    }
}
