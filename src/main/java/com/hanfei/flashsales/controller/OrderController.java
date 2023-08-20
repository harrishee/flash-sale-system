package com.hanfei.flashsales.controller;

import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.OrderService;
import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/detail")
    public Result detail(User user, Long orderNo) {
        if (user == null) {
            return Result.error(ResultEnum.SESSION_ERROR);
        }
        Order order = orderService.getOrderByOrderNo(String.valueOf(orderNo));
        return Result.success(order);
    }
}
