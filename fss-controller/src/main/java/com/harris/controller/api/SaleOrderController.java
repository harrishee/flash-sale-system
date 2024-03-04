package com.harris.controller.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.service.app.SaleOrderAppService;
import com.harris.controller.model.converter.SaleOrderConverter;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.request.PlaceOrderRequest;
import com.harris.controller.model.response.SaleOrderResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
@RequestMapping("/sale-orders")
public class SaleOrderController {
    @Resource
    private SaleOrderAppService saleOrderAppService;
    
    // 根据订单ID获取下单任务结果
    @GetMapping("/items/{itemId}/{placeOrderTaskId}")
    public SingleResponse<OrderHandleResult> getPlaceOrderTaskResult(@RequestAttribute Long userId,
                                                                     @PathVariable Long itemId,
                                                                     @PathVariable String placeOrderTaskId) {
        // 调用应用层的 获取下单任务结果 方法
        AppSingleResult<OrderHandleResult> orderResult = saleOrderAppService.getPlaceOrderTaskResult(userId, itemId, placeOrderTaskId);
        OrderHandleResult orderHandleResult = orderResult.getData();
        
        // 检查获取下单任务结果是否 失败 或者 为空
        return !orderResult.isSuccess() || orderHandleResult == null
                ? ResponseConverter.toSingleResponse(orderResult)
                : SingleResponse.of(orderHandleResult);
    }
    
    // 获取用户订单列表
    @GetMapping("/user")
    public MultiResponse<SaleOrderResponse> listUserOrders(@RequestAttribute Long userId,
                                                           @RequestParam Integer pageSize,
                                                           @RequestParam Integer pageNumber,
                                                           @RequestParam(required = false) String keyword) {
        SaleOrdersQuery ordersQuery = new SaleOrdersQuery().setKeyword(keyword).setPageSize(pageSize).setPageNumber(pageNumber);
        
        // 调用应用层的 获取用户订单列表 方法
        AppMultiResult<SaleOrderDTO> ordersResult = saleOrderAppService.listOrdersByUser(userId, ordersQuery);
        Collection<SaleOrderDTO> orderDTOs = ordersResult.getData();
        Collection<SaleOrderResponse> orderResponses = SaleOrderConverter.toResponseList(orderDTOs);
        
        // 检查获取用户订单列表是否 失败 或者 为空
        return !ordersResult.isSuccess() || orderDTOs == null
                ? ResponseConverter.toMultiResponse(ordersResult)
                : MultiResponse.of(orderResponses, ordersResult.getTotal());
    }
    
    // 下单
    @PostMapping
    public SingleResponse<PlaceOrderResult> placeOrder(@RequestAttribute Long userId, @RequestBody PlaceOrderRequest placeOrderRequest) {
        // 调用应用层的 下单 方法
        PlaceOrderCommand placeOrderCommand = SaleOrderConverter.toCommand(placeOrderRequest);
        AppSingleResult<PlaceOrderResult> placeOrderResult = saleOrderAppService.placeOrder(userId, placeOrderCommand);
        
        // 检查下单结果是否 失败 或者 为空
        return !placeOrderResult.isSuccess() || placeOrderResult.getData() == null
                ? ResponseConverter.toSingleResponse(placeOrderResult)
                : SingleResponse.of(placeOrderResult.getData());
    }
    
    // 取消订单
    @PutMapping("/{orderId}")
    public Response cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        // 调用应用层的 取消订单 方法
        AppResult cancelResult = saleOrderAppService.cancelOrder(userId, orderId);
        return ResponseConverter.toResponse(cancelResult);
    }
}
