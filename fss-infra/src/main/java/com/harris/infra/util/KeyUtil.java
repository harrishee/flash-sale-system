package com.harris.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyUtil {
    
    // 用于将多个对象链接为一个下划线分隔的字符串，用作 分布式锁 的 key
    public static String link(Object... items) {
        if (items == null) {
            return null;
        }
        
        // 遍历所有对象，将它们连接成一个字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            sb.append((items[i]));
            // 除了最后一个对象外，在对象之间插入下划线
            if (i < items.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}
