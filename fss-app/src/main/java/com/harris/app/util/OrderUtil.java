package com.harris.app.util;

import com.harris.infra.util.SnowflakeIdWorker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderUtil {
    private static final SnowflakeIdWorker snowflakeIdWorker;

    /**
     * Initialize snowflakeIdWorker instance
     * <p>
     * Should dynamically get machine id when deploy in distributed environment
     * Here use random number as machine id for now
     * TODO: get machine id dynamically
     */
    static {
        Random random = new Random(1);
        snowflakeIdWorker = new SnowflakeIdWorker(random.nextInt(32), 1, 1);
    }

    /**
     * Generate order number by snowflake algorithm
     *
     * @return order number
     */
    public static Long generateOrderNo() {
        return snowflakeIdWorker.nextId();
    }

    /**
     * Generate order task id by MD5 algorithm
     *
     * @param userId User id
     * @param itemId Item id
     * @return order task id
     */
    public static String generateOrderTaskId(Long userId, Long itemId) {
        String toEncrypt = userId + "_" + itemId;
        return DigestUtils.md5DigestAsHex(toEncrypt.getBytes());
    }
}
