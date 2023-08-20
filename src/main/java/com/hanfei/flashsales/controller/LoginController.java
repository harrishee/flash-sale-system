package com.hanfei.flashsales.controller;

import com.hanfei.flashsales.service.UserService;
import com.hanfei.flashsales.vo.LoginVO;
import com.hanfei.flashsales.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String toLogin() {
        return "login";
    }

    @ResponseBody
    @PostMapping("/processLogin")
    public Result processLogin(@Valid LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) {
        return userService.processLogin(loginVO, request, response);
    }
}
