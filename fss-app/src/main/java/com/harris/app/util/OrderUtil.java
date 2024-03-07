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
        // 实例化SnowflakeIdWorker，参数分别为机器ID、数据中心ID和序列号，这里随机生成机器ID，数据中心ID和序列号都设置为1
        Random random = new Random(1);
        snowflakeIdWorker = new SnowflakeIdWorker(random.nextInt(32), 1, 1);
    }
    
    public static Long generateOrderNo() {
        return snowflakeIdWorker.nextId();
    }
    
    public static String getPlaceOrderTaskId(Long userId, Long itemId) {
        String placeOrderTaskId = userId + "_" + itemId;
        // 使用 MD5 进行加密，并返回加密后的 16 进制字符串作为 订单任务ID
        return DigestUtils.md5DigestAsHex(placeOrderTaskId.getBytes());
    }
}
