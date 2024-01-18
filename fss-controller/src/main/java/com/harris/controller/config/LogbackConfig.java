package com.harris.controller.config;

import com.harris.controller.interceptor.LogbackInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LogbackConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add the logback interceptor and set the path patterns to all paths
        InterceptorRegistration registration = registry.addInterceptor(new LogbackInterceptor());
        registration.addPathPatterns("/**");
    }
}
