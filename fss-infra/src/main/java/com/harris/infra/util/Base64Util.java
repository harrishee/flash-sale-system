package com.harris.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Util {
    private static final String ENCODING = "UTF-8";
    
    // 用于解码 user 的 token
    public static String decode(String data) throws Exception {
        // 将传入的 Base64 编码的字符串解码成字节数组
        byte[] b = Base64.decodeBase64(data.getBytes(ENCODING));
        // 将字节数组转换回原始字符串并返回
        return new String(b, ENCODING);
    }
}
