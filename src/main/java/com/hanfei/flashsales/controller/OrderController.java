package com.hanfei.flashsales.controller;

import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.OrderService;
import com.hanfei.flashsales.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Handle getting order details requests
     */
    @GetMapping("/detail")
    public Result detail(User user, Long orderNo) {
        Order order = orderService.getOrderByOrderNo(String.valueOf(orderNo));
        return Result.success(order);
    }
}
