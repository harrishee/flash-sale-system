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
        // 创建 LogbackInterceptor 的实例并注册到 Spring MVC 的拦截器注册表中
        InterceptorRegistration registration = registry.addInterceptor(new LogbackInterceptor());
        // 设置这个拦截器应用到所有的路径上
        registration.addPathPatterns("/**");
    }
}
