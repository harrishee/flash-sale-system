package com.hanfei.flashsales.utils;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public class SnowFlakeUtils {

    // 起始的时间戳
    private final static long START_STMP = 1480166465631L;

    // 每一部分占用的位数
    private final static long SEQUENCE_BIT = 12; // 序列号占用的位数
    private final static long MACHINE_BIT = 5; // 机器标识占用的位数
    private final static long DATACENTER_BIT = 5; // 数据中心占用的位数

    // 数据中心ID的最大值，用于限制数据中心ID所占的位数不超过DATACENTER_BIT设定的值
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);

    // 机器ID的最大值，用于限制机器ID所占的位数不超过MACHINE_BIT设定的值
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);

    // 序列号的最大值，用于限制序列号所占的位数不超过SEQUENCE_BIT设定的值
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    // 机器标识部分在生成的ID中所占的位移量，表示机器标识部分的值在ID中占用了SEQUENCE_BIT个低位。
    private final static long MACHINE_LEFT = SEQUENCE_BIT;

    // 数据中心部分在生成的ID中所占的位移量，表示数据中心部分的值在ID中占用了MACHINE_BIT+SEQUENCE_BIT个低位。
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    // 时间戳部分在生成的ID中所占的位移量，表示时间戳部分的值在ID中占用了DATACENTER_BIT+MACHINE_BIT+SEQUENCE_BIT个低位。
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId; // 数据中心
    private long machineId; // 机器标识
    private long sequence = 0L; // 序列号
    private long lastStamp = -1L; // 上一次时间戳

    /**
     * SnowFlakeUtils 对象构造函数
     */
    public SnowFlakeUtils(long datacenterId, long machineId) {
        // 判断数据中心ID是否合法，即是否在0到MAX_DATACENTER_NUM之间
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 不在 0 到 MAX_DATACENTER_NUM 之间");
        }

        // 判断机器ID是否合法，即是否在0到MAX_MACHINE_NUM之间
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId 不在 0 到 MAX_MACHINE_NUM 之间");
        }

        // 将传入的数据中心ID和机器ID赋值给SnowFlake对象的成员变量
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 生成下一个唯一ID
     */
    public synchronized long nextId() {
        long currentStamp = getCurrentStamp();

        // 判断当前时间戳是否小于上一次记录的时间戳，如果是，则表示系统时钟发生了回退，抛出异常
        if (currentStamp < lastStamp) {
            throw new RuntimeException("系统时钟发生了回退，拒绝生成 ID");
        }

        // 如果当前时间戳与上一次记录的时间戳相同，则在同一毫秒内生成ID，序列号自增
        if (currentStamp == lastStamp) {
            // 相同毫秒内，序列号自增，并取其低位部分（与MAX_SEQUENCE进行与操作，使序列号保持在有效范围内）
            sequence = (sequence + 1) & MAX_SEQUENCE;

            // 同一毫秒内的序列号已经达到最大值，需要等待下一毫秒再生成ID
            if (sequence == 0L) {
                currentStamp = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }

        // 记录当前时间戳为上一次的时间戳，以便下次生成ID时使用
        lastStamp = currentStamp;

        // 将时间戳、数据中心ID、机器ID和序列号按位左移并进行位或操作，生成最终的唯一ID
        return (currentStamp - START_STMP) << TIMESTMP_LEFT // 时间戳部分
                | datacenterId << DATACENTER_LEFT       // 数据中心部分
                | machineId << MACHINE_LEFT             // 机器标识部分
                | sequence;                             // 序列号部分
    }

    /**
     * 获取下一个合适的毫秒时间戳，确保不小于上一次记录的时间戳
     */
    private long getNextMill() {
        long currentMill = getCurrentStamp();

        // 循环等待，直到获取的毫秒时间戳大于上一次记录的时间戳
        while (currentMill <= lastStamp) {
            currentMill = getCurrentStamp();
        }

        return currentMill;
    }

    /**
     * 获取当前的毫秒时间戳
     */
    private long getCurrentStamp() {
        return System.currentTimeMillis();
    }
}
