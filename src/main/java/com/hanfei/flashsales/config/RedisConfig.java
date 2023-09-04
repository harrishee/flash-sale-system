package com.hanfei.flashsales.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration class used for configuring the RedisTemplate
 *
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Configuration
public class RedisConfig {

    /**
     * Configure the RedisTemplate with specific serializers
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // Set the key serializer to StringRedisSerializer to serialize keys as strings
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Set the value serializer to GenericJackson2JsonRedisSerializer to serialize values in JSON format
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Set the key serializer for hash types to StringRedisSerializer
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Set the value serializer for hash types to GenericJackson2JsonRedisSerializer
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Set the connection factory to allow RedisTemplate to connect to the Redis
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * Load the Lua script for stock deduction
     */
    @Bean
    public DefaultRedisScript<Long> script() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("stockDeductValidator.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
