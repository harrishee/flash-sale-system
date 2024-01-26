package com.harris.infra.config;

import com.alibaba.cola.boot.SpringBootstrap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"com.alibaba.cola"})
public class ColaConfig {
    /**
     * Configures the Alibaba COLA framework within a Spring application context.
     * This configuration scans for components within the 'com.alibaba.cola' package
     * and initializes a SpringBootstrap bean to facilitate the COLA bootstrap process.
     *
     * @return An instance of SpringBootstrap to initialize COLA framework
     */
    @Bean(initMethod = "init")
    public SpringBootstrap bootstrap() {
        return new SpringBootstrap();
    }
}
