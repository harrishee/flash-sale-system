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
public class SecurityRuleInterceptor implements HandlerInterceptor {
    @Resource
    private List<SecurityChainService> securityChainServices; // 注入一系列的安全链服务
    
    private List<SecurityChainService> getSortedSecurityRules() {
        // 获取安全链服务列表
        if (CollectionUtils.isEmpty(securityChainServices)) return new ArrayList<>();
        
        // 根据服务的顺序对安全链服务进行排序
        return securityChainServices.stream().sorted(Comparator.comparing(SecurityChainService::getOrder)).collect(Collectors.toList());
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 遍历排序后的安全链服务
        for (SecurityChainService securityChainService : getSortedSecurityRules()) {
            // 如果任何一个安全链服务返回false，则拦截请求
            if (!securityChainService.run(request, response)) return false;
        }
        
        return true; // 所有安全链服务都通过后，继续执行后续处理流程
    }
}
