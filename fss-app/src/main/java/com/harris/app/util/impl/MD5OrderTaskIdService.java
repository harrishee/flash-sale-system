package com.harris.app.util.impl;

import com.harris.app.util.OrderTaskIdService;
import org.springframework.util.DigestUtils;

public class MD5OrderTaskIdService implements OrderTaskIdService {
    @Override
    public String generateOrderTaskId(Long userId, Long itemId) {
        String toEncrypt = userId + "_" + itemId;
        return DigestUtils.md5DigestAsHex(toEncrypt.getBytes());
    }
}
