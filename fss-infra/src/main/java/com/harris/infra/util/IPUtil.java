package com.harris.infra.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Slf4j
public class IPUtil {
    private static final String IP_UTIL_FLAG = ",";
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP1 = "127.0.0.1";

    private IPUtil() {
    }

    public static String getIpAddr(HttpServletRequest req) {
        String ip = null;
        try {
            ip = req.getHeader("X-Original-Forwarded-For");
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getHeader("X-Forwarded-For");
            }

            // Repeats the process for different headers that might contain the IP
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

            // If IP is still unknown, get it from the remote address of the request
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = req.getRemoteAddr();
                ip = getRealIpAddress(ip);
            }
        } catch (Exception e) {
            log.error("IPUtil error: ", e);
        }

        // Checks if IP contains a comma and splits it to get the first IP
        if (!StringUtils.isEmpty(ip) && ip.contains(IP_UTIL_FLAG)) {
            ip = ip.substring(0, ip.indexOf(IP_UTIL_FLAG));
        }
        return ip;
    }

    private static String getRealIpAddress(String ip) {
        if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
            try {
                InetAddress in = InetAddress.getLocalHost();
                return in.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("Failed to get local host address", e);
            }
        }
        return ip;
    }
}
