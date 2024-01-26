package com.harris.infra.lock;

/**
 * Allows acquiring locks based on a unique key.
 */
public interface DistributedLockService {
    /**
     * Retrieves a distributed lock based on a specified key.
     *
     * @param key The key associated with the lock. This key is used to uniquely identify the lock
     * @return An instance of DistributedLock corresponding to the given key
     */
    DistributedLock getDistributedLock(String key);
}
