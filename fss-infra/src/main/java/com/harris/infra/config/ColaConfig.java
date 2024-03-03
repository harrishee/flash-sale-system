package com.harris.infra.config;

import com.alibaba.cola.boot.SpringBootstrap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.alibaba.cola"})
public class ColaConfig {
    @Bean(initMethod = "init")
    public SpringBootstrap bootstrap() {
        // 初始化COLA框架的启动类，负责框架的初始化工作
        return new SpringBootstrap();
    }
}
