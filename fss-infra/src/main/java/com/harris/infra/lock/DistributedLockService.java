package com.harris.infra.lock;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public interface DistributedLockService {
    DistributedLock getDistributedLock(String key);
}
