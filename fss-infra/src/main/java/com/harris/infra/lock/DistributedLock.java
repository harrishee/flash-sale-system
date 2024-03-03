package com.harris.infra.lock;

import java.util.concurrent.TimeUnit;

// 分布式锁接口，提供了分布式环境下锁的基本操作
public interface DistributedLock {
    // 尝试获取锁，带有等待时间和租约时间
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;
    
    // 获取锁，并设置租约时间
    void lock(long leaseTime, TimeUnit unit);
    
    // 释放锁
    void unlock();
    
    // 判断锁是否被获取
    boolean isLocked();
    
    // 判断锁是否被当前线程持有
    boolean isHeldByCurrentThread();
}
