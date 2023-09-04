package com.hanfei.flashsales.utils;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public class SnowFlakeUtils {

    // Starting timestamp
    private final static long START_STMP = 1480166465631L;

    // Number of bits occupied by each part
    private final static long SEQUENCE_BIT = 12;
    private final static long MACHINE_BIT = 5;
    private final static long DATACENTER_BIT = 5;

    // Maximum value of each part
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    // Left shift of each part
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;
    private long machineId;
    private long sequence = 0L;
    private long lastStamp = -1L;

    public SnowFlakeUtils(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID not in range");
        }

        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("Machine ID not in range");
        }

        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * Generate next ID
     */
    public synchronized long nextId() {
        long currentStamp = getCurrentStamp();
        if (currentStamp < lastStamp) {
            throw new RuntimeException("Clock moved backward. Refusing to generate ID");
        }

        // If the current timestamp is the same as the last recorded timestamp,
        // generate an ID within the same millisecond, incrementing the sequence number
        if (currentStamp == lastStamp) {
            // Within the same millisecond, increment the sequence number and take its lower bits,
            // using bitwise AND with MAX_SEQUENCE to keep it within the valid range
            sequence = (sequence + 1) & MAX_SEQUENCE;

            // If the sequence number within the same millisecond reaches its maximum value,
            // wait for the next millisecond to generate an ID
            if (sequence == 0L) {
                currentStamp = getNextMill();
            }
        } else {
            // Reset the sequence number to 0 if not within the same millisecond
            sequence = 0L;
        }

        lastStamp = currentStamp;

        // Generate the final unique ID by shifting the timestamp, data center ID, machine ID, and sequence number
        // to their respective positions and performing a bitwise OR operation
        return (currentStamp - START_STMP) << TIMESTMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    /**
     * Get next millisecond timestamp, ensuring that it is not less than the last recorded timestamp
     */
    private long getNextMill() {
        long currentMill = getCurrentStamp();

        // Wait in a loop until the millisecond timestamp obtained is greater than the last recorded timestamp
        while (currentMill <= lastStamp) {
            currentMill = getCurrentStamp();
        }
        return currentMill;
    }

    private long getCurrentStamp() {
        return System.currentTimeMillis();
    }
}
