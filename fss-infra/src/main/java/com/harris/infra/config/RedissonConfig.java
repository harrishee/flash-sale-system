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

    /**
     * Creates a RedissonClient bean for interacting with Redis.
     * This client provides various Redis operations and uses a single server configuration.
     *
     * @return Configured RedissonClient instance
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Configuring Redisson to use a single Redis server.
        // The address of the Redis server and the database index (0 in this case) are set here.
        config.useSingleServer().setAddress(redisAddress).setDatabase(0);
        return Redisson.create(config);
    }
}
