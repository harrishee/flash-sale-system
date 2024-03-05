package com.harris.app.util;

import com.harris.infra.util.SnowflakeIdWorker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderUtil {
    private static final SnowflakeIdWorker snowflakeIdWorker;
    
    static {
        // 创建一个Random实例，种子为1，用于生成机器ID
        Random random = new Random(1);
        // 实例化SnowflakeIdWorker，参数分别为机器ID、数据中心ID和序列号，这里随机生成机器ID，数据中心ID和序列号都设置为1
        snowflakeIdWorker = new SnowflakeIdWorker(random.nextInt(32), 1, 1);
    }
    
    public static Long generateOrderNo() {
        // // 用 Snowflake 算法生成唯一的订单号
        return snowflakeIdWorker.nextId();
    }
    
    public static String getPlaceOrderTaskId(Long userId, Long itemId) {
        // 将 用户ID 和 商品ID 拼接成一个字符串，中间用下划线分隔
        String toEncrypt = userId + "_" + itemId;
        // 使用MD5算法对拼接后的字符串进行加密，并返回加密后的 16 进制字符串作为 订单任务ID
        return DigestUtils.md5DigestAsHex(toEncrypt.getBytes());
    }
}
