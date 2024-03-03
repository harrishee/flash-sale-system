package com.harris.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IPUtil {
    private static final String IP_UTIL_FLAG = ",";
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP1 = "127.0.0.1";
    
    // 从 HttpServletRequest 中获取 IP 地址
    public static String getIpAddr(HttpServletRequest req) {
        String ip = null;
        try {
            // 尝试从各种HTTP头中获取IP地址
            ip = req.getHeader("X-Original-Forwarded-For");
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("X-Forwarded-For");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("x-forwarded-for");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("HTTP_X_FORWARDED_FOR");
            }
            
            // 如果从常规头中未获取到IP，尝试从请求的远程地址中获取
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                // 如果从常规头中未获取到IP，最后尝试从请求的远程地址中获取
                ip = req.getRemoteAddr();
                // 将获取到的IP地址进行最终处理并返回
                ip = getRealIpAddress(ip);
            }
        } catch (Exception e) {
            log.error("IPUtil，获取IP地址出错", e);
        }
        
        // 检查是否有多个IP地址，如果有则只取第一个
        if (!StringUtils.isEmpty(ip) && ip.contains(IP_UTIL_FLAG)) {
            ip = ip.substring(0, ip.indexOf(IP_UTIL_FLAG));
        }
        return ip;
    }
    
    private static String getRealIpAddress(String ip) {
        // 如果获取到的IP地址是本地地址，则尝试获取本机的真实IP地址
        if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
            try {
                InetAddress in = InetAddress.getLocalHost();
                return in.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("IPUtil，获取IP地址出错", e);
            }
        }
        return ip;
    }
}
