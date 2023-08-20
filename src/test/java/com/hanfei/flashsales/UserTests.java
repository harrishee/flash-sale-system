package com.hanfei.flashsales;

import com.hanfei.flashsales.utils.UserUtils;
import org.junit.jupiter.api.Test;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
public class UserTests {

    @Test
    void generateUsers() throws Exception {
        UserUtils.createUser(3000);
    }
}
