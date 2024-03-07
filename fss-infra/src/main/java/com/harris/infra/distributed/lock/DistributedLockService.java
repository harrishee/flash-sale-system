package com.harris.infra.distributed.lock;

public interface DistributedLockService {
    DistributedLock getLock(String key);
}
