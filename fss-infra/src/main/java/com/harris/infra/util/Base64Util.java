package com.harris.infra.util;

import org.apache.commons.codec.binary.Base64;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public class Base64Util {
    private static final String ENCODING = "UTF-8";

    private Base64Util() {
    }

    public static String encode(String data) throws Exception {
        byte[] b = Base64.encodeBase64(data.getBytes(ENCODING));
        return new String(b, ENCODING);
    }

    public static String decode(String data) throws Exception {
        byte[] b = Base64.decodeBase64(data.getBytes(ENCODING));
        return new String(b, ENCODING);
    }
}
