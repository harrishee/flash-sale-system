package com.harris.infra.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Slf4j
public class SnowFlakeUtil {
    // 下面两个每个5位，加起来就是10位的工作机器id
    private long workerId;    // 工作id
    private long datacenterId;   // 数据id
    private long sequence; // 12位的序列号
    private long twepoch = 1288834974657L; // 初始时间戳
    // 长度为5位
    private long workerIdBits = 5L;
    private long datacenterIdBits = 5L;
    // 最大值
    private long maxWorkerId = ~(-1L << workerIdBits);
    private long maxDatacenterId = ~(-1L << datacenterIdBits);
    private long sequenceBits = 12L; // 序列号id长度
    private long sequenceMask = ~(-1L << sequenceBits); // 序列号最大值
    private long workerIdShift = sequenceBits; // 工作id需要左移的位数，12位
    private long datacenterIdShift = sequenceBits + workerIdBits; // 数据id需要左移位数 12+5=17位
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits; // 时间戳需要左移位数 12+5+5=22位
    private long lastTimestamp = -1L; // 上次时间戳，初始值为负数

    public SnowFlakeUtil(long workerId, long datacenterId, long sequence) {
        if (workerId > maxWorkerId || workerId < 0) {
            String errMsg = String.format("workerId can't be greater than %d or less than 0", maxWorkerId);
            log.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            String errMsg = String.format("datacenterId can't be greater than %d or less than 0", maxDatacenterId);
            log.error(errMsg);
            throw new IllegalArgumentException((errMsg));
        }
        log.info("worker starting: timestampLeftShift {}, datacenterIdBits {}, workerIdBits {}, sequenceBits {}, workerId {}",
                timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId);

        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }

    // 下一个ID生成算法
    public synchronized long nextId() {
        long timestamp = getTimestamp();

        // 获取当前时间戳如果小于上次时间戳，则表示时间戳获取出现异常
        if (timestamp < lastTimestamp) {
            log.error("clock is moving backwards. Rejecting requests until {}", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        // 获取当前时间戳如果等于上次时间戳（同一毫秒内），则在序列号加一；否则序列号赋值为0，从0开始。
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        // 将上次时间戳值刷新
        lastTimestamp = timestamp;

        /*
          返回结果：
          (timestamp - twepoch) << timestampLeftShift) 表示将时间戳减去初始时间戳，再左移相应位数
          (datacenterId << datacenterIdShift) 表示将数据id左移相应位数
          (workerId << workerIdShift) 表示将工作id左移相应位数
          | 是按位或运算符，例如：x | y，只有当x，y都为0的时候结果才为0，其它情况结果都为1。
          因为个部分只有相应位上的值有意义，其它位上都是0，所以将各部分的值进行 | 运算就能得到最终拼接好的id
         */
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    // 获取时间戳，并与上次时间戳比较
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = getTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getTimestamp();
        }
        return timestamp;
    }

    // 获取系统时间戳
    private long getTimestamp() {
        return System.currentTimeMillis();
    }
}
