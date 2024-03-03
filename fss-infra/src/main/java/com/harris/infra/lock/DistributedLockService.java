package com.harris.infra.lock;

// 分布式锁服务接口，用于获取分布式锁
public interface DistributedLockService {
    // 根据给定的键获取一个分布式锁实例
    DistributedLock getDistributedLock(String key);
}
