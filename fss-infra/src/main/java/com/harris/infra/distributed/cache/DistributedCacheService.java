package com.harris.infra.distributed.cache;

import java.util.concurrent.TimeUnit;

public interface DistributedCacheService {
    void put(String key, String value);
    
    void put(String key, Object value);
    
    void put(String key, Object value, long timeout);
    
    void put(String key, Object value, long timeout, TimeUnit unit);
    
    <T> T get(String key, Class<T> targetClass);
}
