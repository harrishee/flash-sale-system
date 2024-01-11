package com.harris.infra.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public interface DistributedLock {
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    void lock(long leaseTime, TimeUnit unit);

    void unlock();

    boolean isLocked();

    boolean isHeldByCurrentThread();
}
