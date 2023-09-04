package com.hanfei.flashsales.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hanfei.flashsales.exception.GlobalException;
import com.hanfei.flashsales.mapper.UserMapper;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.UserService;
import com.hanfei.flashsales.utils.CookieUtils;
import com.hanfei.flashsales.utils.MD5Utils;
import com.hanfei.flashsales.utils.UUIDUtils;
import com.hanfei.flashsales.vo.LoginVO;
import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result processLogin(LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) {
        String userId = loginVO.getUserId();
        String password = loginVO.getPassword();

        // 1. Get user from mysql with userId
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new GlobalException(ResultEnum.USER_ID_NOT_EXIST);
        }

        // 2. Verify password
        if (!MD5Utils.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(ResultEnum.LOGIN_ERROR);
        }

        // 3. Generate ticket using UUID
        String ticket = UUIDUtils.generateUUID();

        // 4. Set user:ticket as key, user information as value, and store it in Redis
        redisTemplate.opsForValue().set("user:" + ticket, JSONObject.toJSONString(user));

        // 5. Set ticket as Cookie and return it to the client
        CookieUtils.setCookie(request, response, "ticket", ticket);

        log.info("Login success, userId: [{}], ticket: [{}]", userId, ticket);
        return Result.success(ticket);
    }

    /**
     * Retrieve a User object by ticket
     */
    @Override
    public User getUserByTicket(String ticket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(ticket)) {
            return null;
        }

        // Retrieve user information from Redis based on the provided ticket
        String userJson = (String) redisTemplate.opsForValue().get("user:" + ticket);
        User user = JSONObject.parseObject(userJson, User.class);

        // Update the user's Cookie to extend the session's validity if the user is found
        if (user != null) {
            CookieUtils.setCookie(request, response, "ticket", ticket);
        }
        return user;
    }
}
