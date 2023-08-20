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
 * @summary: seckill
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

        // 数据库查询用户
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new GlobalException(ResultEnum.USER_ID_NOT_EXIST);
        }

        // 校验用户密码
        if (!MD5Utils.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(ResultEnum.LOGIN_ERROR);
        }

        // 用 UUID 生成 ticket
        String ticket = UUIDUtils.generateUUID();

        // 将 user:ticket 作为 key，用户信息作为 value，存入 Redis 中
        redisTemplate.opsForValue().set("user:" + ticket, JSONObject.toJSONString(user));

        // 把 ticket 作为 Cookie，返回给客户端
        CookieUtils.setCookie(request, response, "ticket", ticket);

        log.info("***Service*** 登陆成功，用户ID：{} 已生成 ticket：{}", userId, ticket);
        return Result.success(ticket);
    }

    @Override
    public User getUserByTicket(String ticket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(ticket)) {
            return null;
        }

        // 根据 ticket 从 Redis 中获取用户信息
        String userJson = (String) redisTemplate.opsForValue().get("user:" + ticket);
        User user = JSONObject.parseObject(userJson, User.class);

        // 更新用户的 Cookie，延长用户会话的有效期
        if (user != null) {
            CookieUtils.setCookie(request, response, "ticket", ticket);
        }
        return user;
    }
}
