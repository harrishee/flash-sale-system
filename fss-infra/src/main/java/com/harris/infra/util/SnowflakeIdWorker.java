package com.harris.infra.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeIdWorker {
    private long workerId;
    private long datacenterId;
    private long sequence;
    private long twepoch = 1288834974657L;
    private long workerIdBits = 5L;
    private long datacenterIdBits = 5L;
    private long maxWorkerId = ~(-1L << workerIdBits);
    private long maxDatacenterId = ~(-1L << datacenterIdBits);
    private long sequenceBits = 12L;
    private long sequenceMask = ~(-1L << sequenceBits);
    private long workerIdShift = sequenceBits;
    private long datacenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private long lastTimestamp = -1L;

    public SnowflakeIdWorker(long workerId, long datacenterId, long sequence) {
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

    public synchronized long nextId() {
        long timestamp = getTimestamp();

        if (timestamp < lastTimestamp) {
            log.error("clock is moving backwards. Rejecting requests until {}", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = getTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getTimestamp();
        }
        return timestamp;
    }

    private long getTimestamp() {
        return System.currentTimeMillis();
    }
}
