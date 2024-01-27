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
    private AuthInterceptor authInterceptor;

    @Resource
    private SecurityRuleInterceptor securityRuleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add the authentication interceptor and set the path patterns to all paths
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        // Add the security rules interceptor and set the path patterns to all paths
        registry.addInterceptor(securityRuleInterceptor).addPathPatterns("/**");
    }
}
