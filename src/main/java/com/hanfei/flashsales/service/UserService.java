package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.vo.LoginVO;
import com.hanfei.flashsales.vo.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
public interface UserService {

    Result processLogin(LoginVO loginVO, HttpServletRequest request, HttpServletResponse response);

    User getUserByTicket(String ticket, HttpServletRequest request, HttpServletResponse response);
}
