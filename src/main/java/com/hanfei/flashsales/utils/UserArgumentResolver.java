package com.hanfei.flashsales.utils;

import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.UserService;
import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Custom User Parameter Resolver
 *
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService;

    /**
     * Determine if the resolver supports the User parameter
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return parameterType == User.class;
    }

    /**
     * Resolve the parameter and return a User object
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse nativeResponse = webRequest.getNativeResponse(HttpServletResponse.class);

        // Retrieve the "ticket" value from the request's Cookie
        String ticket = CookieUtils.getCookieValue(nativeRequest, "ticket", false);
        if (StringUtils.isEmpty(ticket)) {
            return Result.error(ResultEnum.SESSION_ERROR);
        }
        return userService.getUserByTicket(ticket, nativeRequest, nativeResponse);
    }
}
