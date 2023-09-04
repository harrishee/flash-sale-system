package com.hanfei.flashsales.utils;

import java.util.UUID;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public class UUIDUtils {

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
