package com.harris.infra.distributed.lock;

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
    public DistributedLock getLock(String key) {
        RLock rLock = redissonClient.getLock(key);
        // log.info("分布式锁实例，获取 Redisson 锁对象: [key={}]", key);
        
        return new DistributedLock() {
            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean lockSuccess = rLock.tryLock(waitTime, leaseTime, unit);
                // log.info("分布式锁，尝试获取锁: [key={}, lockSuccess={}]", key, lockSuccess);
                return lockSuccess;
            }
            
            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                rLock.lock(leaseTime, unit);
                // log.info("分布式锁，成功获取锁: [key={}]", key);
            }
            
            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    rLock.unlock();
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
