package com.student.utils;

import org.springframework.stereotype.Component;

/**
 * 15位雪花ID生成器
 */
@Component
public class SnowflakeIdGenerator {
    // 开始时间戳 (2024-01-01)
    private final long START_TIMESTAMP = 1704038400000L;

    // 每部分占用的位数
    private final long SEQUENCE_BIT = 8;    // 序列号占用8位
    private final long MACHINE_BIT = 4;     // 机器标识占用4位
    private final long DATACENTER_BIT = 4;  // 数据中心占用4位
    private final long TIMESTAMP_BIT = 32;  // 时间戳占用32位

    // 最大值
    private final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);  // 255
    private final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);  // 15
    private final long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);  // 15

    // 时间戳最大差值 ((1L << 32) - 1) / 1000 / 3600 / 24 / 365 ≈ 136年
    private final long MAX_TIMESTAMP = -1L ^ (-1L << TIMESTAMP_BIT);

    private long datacenterId = 1;  // 数据中心ID
    private long machineId = 1;     // 机器标识ID
    private long sequence = 0L;     // 序列号
    private long lastTimestamp = -1L;// 上一次时间戳

    public SnowflakeIdGenerator() {
    }

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("Machine ID can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 获取15位的ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成15位的ID
        long timeDiff = (timestamp - START_TIMESTAMP) & MAX_TIMESTAMP;
        
        // 压缩成15位数字
        return (timeDiff % 100000000L) * 10000000L + // 8位时间戳
               (datacenterId % 10L) * 1000000L +     // 1位数据中心ID
               (machineId % 10L) * 100000L +         // 1位机器ID
               (sequence % 100000L);                  // 5位序列号
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回当前时间戳
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
