package com.hanfei.flashsales.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Component
public class MD5Utils {

    private static final String salt = "1a2b3c4d";

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    // First MD5: User's input password + Fixed salt -> Form password
    public static String inputPassToFormPass(String input) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + input + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    // Second MD5: Form password + User salt -> Database password
    public static String formPassToDBPass(String pass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + pass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    // Directly Two MD5: User's input password + Fixed salt + User salt -> Database password
    public static String inputPassToDBPass(String input, String salt) {
        String pass = inputPassToFormPass(input);
        return formPassToDBPass(pass, salt);
    }
}
