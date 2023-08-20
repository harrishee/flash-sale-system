package com.hanfei.flashsales.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
public class CookieUtils {


    /**
     * 得到 Cookie 的值，根据参数 isDecoder 来决定是否进行 "UTF-8" URL 解码
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }

        String res = null;
        for (Cookie cookie : cookieList) {
            if (cookie.getName().equals(cookieName)) {
                if (isDecoder) {
                    // 如果需要解码，对 Cookie 值进行 URL 解码
                    res = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                } else {
                    // 否则直接使用原始 Cookie 值
                    res = cookie.getValue();
                }
                break;
            }
        }
        return res;
    }

    /**
     * 设置 Cookie，不编码，不设置有效时间，默认浏览器关闭失效
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
                                 String name, String value) {
        setCookieHelper(request, response, name, value, -1, false);
    }

    /**
     * 设置 Cookie，支持选择是否编码和设置域名
     *
     * @param request  HttpServletRequest 对象，用于确定域名
     * @param response HttpServletResponse 对象，用于添加 Cookie
     * @param name     要设置的 Cookie 的名称
     * @param value    要存储在 Cookie 中的值
     * @param maxAge   Cookie 的最大有效时间，以秒为单位
     * @param isEncode 是否对 Cookie 值进行编码
     */
    private static void setCookieHelper(HttpServletRequest request, HttpServletResponse response,
                                        String name, String value, int maxAge, boolean isEncode) {
        try {
            if (value == null) {
                value = "";
            } else if (isEncode) {
                value = URLEncoder.encode(value, StandardCharsets.UTF_8);
            }

            Cookie cookie = new Cookie(name, value);
            if (maxAge > 0)
                cookie.setMaxAge(maxAge);

            // 设置Cookie的域名
            if (request != null) {
                String domainName = request.getServerName();
                if (!domainName.equals("localhost")) {
                    cookie.setDomain(domainName);
                }
            }

            // 设置 Cookie 的路径，'/' 表示在整个应用程序中都可以访问该 Cookie
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error("设置 Cookie 异常：", e);
        }
    }
}
