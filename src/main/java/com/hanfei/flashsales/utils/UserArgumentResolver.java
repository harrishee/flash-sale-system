package com.hanfei.flashsales.utils;

import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.UserService;
import com.hanfei.flashsales.utils.CookieUtils;
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
 * 自定义用户参数解析器
 * 通过从 Cookie 中获取 userTicket，并通过该标识从 UserService 中获取对应的 User 对象
 * 实现了将 User 对象作为参数注入到控制器方法中的功能
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
     * 判断是否支持 User 参数的解析
     *
     * @param parameter 方法参数
     * @return 如果参数类型为 User.class 则返回 true，表示支持解析，否则返回 false
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return parameterType == User.class;
    }

    /**
     * 解析参数并返回 User 对象
     *
     * @param parameter     方法参数
     * @param mavContainer  ModelAndViewContainer 对象
     * @param webRequest    NativeWebRequest 对象，提供对原生 HttpServletRequest 和 HttpServletResponse 的访问
     * @param binderFactory WebDataBinderFactory 对象
     * @return 解析后的 User 对象，如果解析失败或用户未登录则返回 null
     * @throws Exception 如果解析过程中出现异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse nativeResponse = webRequest.getNativeResponse(HttpServletResponse.class);

        // 从请求的 Cookie 中获取名为 "userTicket" 的 Cookie 值
        String ticket = CookieUtils.getCookieValue(nativeRequest, "ticket", false);

        // 如果用户票据为空，说明用户未登录，直接返回 null
        // StringUtils.isEmpty 和 userTicket == null || userTicket.isEmpty() 一样
        if (StringUtils.isEmpty(ticket)) {
            return null;
        }
        return userService.getUserByTicket(ticket, nativeRequest, nativeResponse);
    }
}
