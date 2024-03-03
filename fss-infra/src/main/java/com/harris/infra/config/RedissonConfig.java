package com.harris.infra.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${spring.redis.address}")
    private String redisAddress;
    
    @Bean
    public RedissonClient redissonClient() {
        // 配置并创建RedissonClient实例，用于操作Redis
        Config config = new Config();
        config.useSingleServer().setAddress(redisAddress).setDatabase(0);
        return Redisson.create(config);
    }
}
