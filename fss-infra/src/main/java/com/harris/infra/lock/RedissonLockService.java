package com.harris.infra.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedissonLockService implements DistributedLockService {
    @Resource
    private RedissonClient redissonClient;

    @Override
    public DistributedLock getDistributedLock(String key) {
        RLock rLock = redissonClient.getLock(key);
        log.info("Acquired Redisson lock for key: {}", key);

        return new DistributedLock() {
            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean lockSuccess = rLock.tryLock(waitTime, leaseTime, unit);
                log.debug("Lock acquire attempt for key: {} returned: {}", key, lockSuccess);
                return lockSuccess;
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                rLock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    rLock.unlock();
                } else {
                    log.warn("Attempted to unlock: {}, but not held by current thread", key);
                }
            }

            @Override
            public boolean isLocked() {
                return rLock.isLocked();
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return rLock.isHeldByCurrentThread();
            }
        };
    }
}
