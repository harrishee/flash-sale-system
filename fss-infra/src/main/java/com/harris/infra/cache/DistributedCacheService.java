package com.harris.infra.cache;

import java.util.concurrent.TimeUnit;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public interface DistributedCacheService {
    void put(String key, String value);

    void put(String key, Object value);

    void put(String key, Object value, long expireTime);

    void put(String key, Object value, long timeout, TimeUnit unit);

    <T> T getObject(String key, Class<T> targetClass);
}
