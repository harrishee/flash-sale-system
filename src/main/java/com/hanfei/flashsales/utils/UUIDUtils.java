package com.hanfei.flashsales.utils;

import java.util.UUID;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public class UUIDUtils {

    public static String generateUUID() {
        // 生成 UUID，并移除其中的横杠（-）
        return UUID.randomUUID().toString().replace("-", "");
    }
}
