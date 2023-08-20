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

    // 第一次 MD5：用户输入的密码 + 固定盐 -> 表单密码
    public static String inputPassToFormPass(String input) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + input + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    // 第二次 MD5：表单密码 + 用户盐 -> 数据库密码
    public static String formPassToDBPass(String pass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + pass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    // 直接两次 MD5：用户输入的密码 + 固定盐 + 用户盐 -> 数据库密码
    public static String inputPassToDBPass(String input, String salt) {
        String pass = inputPassToFormPass(input);
        return formPassToDBPass(pass, salt);
    }
}
