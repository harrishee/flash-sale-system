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
     * Get the value of a cookie, with an option to decode it with "UTF-8"
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
                    // Decode the cookie value with URL decoding if needed
                    res = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                } else {
                    // Otherwise, use the original cookie value
                    res = cookie.getValue();
                }
                break;
            }
        }
        return res;
    }

    /**
     * Set a cookie without encoding, without setting an expiration time (expires when the browser is closed)
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
                                 String name, String value) {
        setCookieHelper(request, response, name, value, -1, false);
    }

    /**
     * Set a cookie with options for encoding, setting an expiration time, and specifying the domain.
     *
     * @param request  HttpServletRequest object.
     * @param response HttpServletResponse object to add the cookie.
     * @param name     Name of the cookie to set.
     * @param value    Value to store in the cookie.
     * @param maxAge   Maximum age of the cookie in seconds (expires after this time).
     * @param isEncode Whether to encode the cookie value.
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

            // Set the cookie's domain
            if (request != null) {
                String domainName = request.getServerName();
                if (!domainName.equals("localhost")) {
                    cookie.setDomain(domainName);
                }
            }

            // Set the cookie path, '/' means it's accessible throughout the entire application
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error("Set Cookie Exception:", e);
        }
    }
}
