package com.harris.infra.config;

import com.alibaba.cola.boot.SpringBootstrap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Configuration
@ComponentScan(value = {"com.alibaba.cola"})
public class ColaConfig {
    @Bean(initMethod = "init")
    public SpringBootstrap bootstrap() {
        return new SpringBootstrap();
    }
}
