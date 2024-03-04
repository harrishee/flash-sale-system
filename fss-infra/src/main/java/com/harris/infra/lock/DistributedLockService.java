package com.harris.infra.lock;

public interface DistributedLockService {
    DistributedLock getLock(String key);
}
