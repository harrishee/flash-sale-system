package com.harris.controller.interceptor;

import com.harris.infra.security.SecurityChainService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityRulesInterceptor implements HandlerInterceptor {
    @Resource
    private List<SecurityChainService> securityChainServices;

    private List<SecurityChainService> getSortedSecurityRules() {
        // Get the list of security rule chain services
        if (CollectionUtils.isEmpty(securityChainServices)) {
            return new ArrayList<>();
        }
        // Sort the security rule chain services based on their order
        return securityChainServices.stream()
                .sorted(Comparator.comparing(SecurityChainService::getOrder))
                .collect(Collectors.toList());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Iterate through the security rule chain services
        for (SecurityChainService securityChainService : getSortedSecurityRules()) {
            if (!securityChainService.run(request, response)) {
                return false;
            }
        }
        return true;
    }
}
