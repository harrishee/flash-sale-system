package com.harris.infra.lock;

public interface DistributedLockService {
    DistributedLock getDistributedLock(String key);
}
