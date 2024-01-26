package com.harris.infra.lock;

import java.util.concurrent.TimeUnit;

/**
 * Provides methods for managing access to a shared resource in a distributed system.
 */
public interface DistributedLock {
    /**
     * Attempts to acquire the lock within a specified waiting time and lease time.
     *
     * @param waitTime  The maximum time to wait for the lock
     * @param leaseTime The time to hold the lock after acquisition
     * @param unit      The time unit of the waitTime and leaseTime parameters
     * @return true if the lock was acquired within the wait time, false otherwise
     * @throws InterruptedException if the current thread is interrupted while waiting for the lock
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    /**
     * Acquires the lock, blocking until it's available or the lease time expires.
     *
     * @param leaseTime The time to hold the lock after acquisition
     * @param unit      The time unit of the leaseTime parameter
     */
    void lock(long leaseTime, TimeUnit unit);

    /**
     * Releases the lock if it's held by the current thread.
     */
    void unlock();

    /**
     * Checks if the lock is currently held by any thread.
     *
     * @return true if the lock is currently held, false otherwise
     */
    boolean isLocked();

    /**
     * Checks if the lock is held by the current thread.
     *
     * @return true if the current thread holds the lock, false otherwise
     */
    boolean isHeldByCurrentThread();
}
