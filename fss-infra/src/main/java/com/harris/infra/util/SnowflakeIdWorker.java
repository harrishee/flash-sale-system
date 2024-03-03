package com.harris.infra.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeIdWorker {
    private long workerId; // 工作机器ID
    private long datacenterId; // 数据中心ID
    private long sequence; // 序列号
    private long twepoch = 1288834974657L; // 开始时间截 (2015-01-01)
    private long workerIdBits = 5L; // 机器id所占的位数
    private long datacenterIdBits = 5L; // 数据中心id所占的位数
    private long maxWorkerId = ~(-1L << workerIdBits); // 支持的最大机器id，结果是31
    private long maxDatacenterId = ~(-1L << datacenterIdBits); // 支持的最大数据中心id，结果是31
    private long sequenceBits = 12L; // 序列在id中占的位数
    private long sequenceMask = ~(-1L << sequenceBits); // 生成序列的掩码，这里为4095
    private long workerIdShift = sequenceBits; // 机器ID向左移12位
    private long datacenterIdShift = sequenceBits + workerIdBits; // 数据中心ID向左移17位(12+5)
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits; // 时间戳向左移22位(5+5+12)
    private long lastTimestamp = -1L; // 上次生成ID的时间截
    
    public SnowflakeIdWorker(long workerId, long datacenterId, long sequence) {
        if (workerId > maxWorkerId || workerId < 0) {
            String errMsg = String.format("workerId 不能大于 %d 或小于 0", maxWorkerId);
            log.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            String errMsg = String.format("datacenterId 不能大于 %d 或小于 0", maxDatacenterId);
            log.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }
    
    public synchronized long nextId() {
        long timestamp = getTimestamp();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回拨，抛出异常
        if (timestamp < lastTimestamp) {
            log.error("系统时钟回拨，拒绝为 %d 毫秒生成 id", lastTimestamp - timestamp);
            throw new RuntimeException(String.format("系统时钟回拨。拒绝为 %d 毫秒生成 id", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0; // 时间戳改变，毫秒内序列重置
        }
        
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }
    
    // 阻塞到下一个毫秒，直到获得新的时间戳
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = getTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getTimestamp();
        }
        return timestamp;
    }
    
    // 返回以毫秒为单位的当前时间
    private long getTimestamp() {
        return System.currentTimeMillis();
    }
}
