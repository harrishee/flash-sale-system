package com.hanfei.flashsales.mapper;

import com.hanfei.flashsales.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Mapper
public interface UserMapper {

    int insertUser(User user);

    User selectUserById(String userId);
}
