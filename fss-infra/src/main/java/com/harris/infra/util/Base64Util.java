package com.harris.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Util {
    private static final String ENCODING = "UTF-8";

    public static String decode(String data) throws Exception {
        byte[] b = Base64.decodeBase64(data.getBytes(ENCODING));
        return new String(b, ENCODING);
    }
}
