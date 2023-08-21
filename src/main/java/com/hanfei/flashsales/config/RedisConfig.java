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
 * Redis配置类，用于配置 RedisTemplate
 *
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Configuration
public class RedisConfig {

    /**
     * 创建一个 RedisTemplate 实例，用于操作 Redis 数据库
     * 将不同类型的数据 序列化 后存储到 Redis 中，以及 反序列化 取出
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // Spring 提供的一个用于操作 Redis 的工具类，提供了一系列操作 Redis 增删改查的方法
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 配置 RedisTemplate 的序列化器，将数据序列化为特定格式进行存储和传输
        // 在存储数据到 Redis 时，数据需要序列化成字节流，而在取出数据时需要反序列化

        // 设置 key 的序列化器为 StringRedisSerializer，将 key 序列化为字符串
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 设置 value 的序列化器为 GenericJackson2JsonRedisSerializer，将 value 序列化为 JSON 格式
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 设置 hash 类型的 key 的序列化器为 StringRedisSerializer
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 设置 hash 类型的 value 的序列化器为 GenericJackson2JsonRedisSerializer
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 设置连接工厂，让 RedisTemplate 能够连接 Redis 数据库
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public DefaultRedisScript<Long> script() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 放在和 application.yml 同层目录下
        redisScript.setLocation(new ClassPathResource("stockDeductValidator.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
