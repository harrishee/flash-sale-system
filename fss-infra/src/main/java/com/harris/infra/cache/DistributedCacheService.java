package com.harris.infra.cache;

import java.util.concurrent.TimeUnit;

public interface DistributedCacheService {
    /**
     * Stores a string value in the cache.
     *
     * @param key   Cache key
     * @param value String value
     */
    void put(String key, String value);

    /**
     * Stores an object in the cache.
     *
     * @param key   Cache key
     * @param value Object to store
     */
    void put(String key, Object value);

    /**
     * Stores an object in the cache with a timeout.
     *
     * @param key     Cache key
     * @param value   Object to store
     * @param timeout Expiration time in seconds
     */
    void put(String key, Object value, long timeout);

    /**
     * Stores an object in the cache with a timeout and time unit.
     *
     * @param key     Cache key
     * @param value   Object to store
     * @param timeout Timeout duration
     * @param unit    Time unit for the timeout
     */
    void put(String key, Object value, long timeout, TimeUnit unit);

    /**
     * Retrieves an object from the cache and converts it to the specified class type.
     *
     * @param key         Cache key
     * @param targetClass Class type for conversion
     * @param <T>         Type parameter
     * @return Converted object or null if not found/convertible
     */
    <T> T getObject(String key, Class<T> targetClass);
}
