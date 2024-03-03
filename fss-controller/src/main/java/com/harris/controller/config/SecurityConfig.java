package com.harris.controller.config;

import com.harris.controller.interceptor.AuthInterceptor;
import com.harris.controller.interceptor.SecurityRuleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Resource
    private AuthInterceptor authInterceptor; // 注入认证拦截器
    
    @Resource
    private SecurityRuleInterceptor securityRuleInterceptor; // 注入安全规则拦截器
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器，并设置其应用到所有的路径上
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        // 注册安全规则拦截器，并设置其也应用到所有的路径上
        registry.addInterceptor(securityRuleInterceptor).addPathPatterns("/**");
    }
}
