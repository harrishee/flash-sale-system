package com.harris.infra.lock.impl;

import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedissonLockServiceImpl implements DistributedLockService {
    @Resource
    private RedissonClient redissonClient;

    @Override
    public DistributedLock getDistributedLock(String key) {
        RLock lock = redissonClient.getLock(key);

        return new DistributedLock() {
            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean isSuccess = lock.tryLock(waitTime, leaseTime, unit);
                log.info("Try to get lock | {}: {}", key, isSuccess);
                return isSuccess;
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                lock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            @Override
            public boolean isLocked() {
                return lock.isLocked();
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return lock.isHeldByCurrentThread();
            }
        };
    }
}
