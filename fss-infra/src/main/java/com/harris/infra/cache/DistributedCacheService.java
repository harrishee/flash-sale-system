package com.harris.infra.cache;

import java.util.concurrent.TimeUnit;

public interface DistributedCacheService {
    // 根据 key 缓存 string 类型的 value
    void put(String key, String value);

    // 根据 key 缓存 object 类型的 value
    void put(String key, Object value);

    // 根据 key 缓存 object 类型的 value，并设置超时时间
    void put(String key, Object value, long timeout);

    // 根据 key 缓存 object 类型的 value，并设置超时时间和时间单位
    void put(String key, Object value, long timeout, TimeUnit unit);
    
    // 根据 key 获取缓存的 object 类型的 value，并转换为指定的 class 类型
    <T> T getObject(String key, Class<T> targetClass);
}
