package com.harris.controller.resource;

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
    @GetMapping("/{orderId}")
    public SingleResponse<OrderHandleResult> getPlaceOrderTaskResult(@RequestAttribute Long userId,
                                                                     @PathVariable Long itemId,
                                                                     @PathVariable String orderId) {
        // 从应用层获取下单任务结果，并转换为 订单处理结果对象
        AppSingleResult<OrderHandleResult> orderResult = saleOrderAppService.getPlaceOrderTaskResult(userId, itemId, orderId);
        OrderHandleResult orderHandleResult = orderResult.getData();
        
        // 如果获取下单任务结果失败或者下单任务结果为空，则返回对应的错误响应；否则返回成功响应并携带下单任务结果
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
        // 构建查询对象
        SaleOrdersQuery ordersQuery = new SaleOrdersQuery().setKeyword(keyword).setPageSize(pageSize).setPageNumber(pageNumber);
        
        // 从应用层获取用户订单列表
        AppMultiResult<SaleOrderDTO> ordersResult = saleOrderAppService.listOrdersByUser(userId, ordersQuery);
        
        // 将用户订单列表转换为响应对象
        Collection<SaleOrderDTO> orderDTOs = ordersResult.getData();
        Collection<SaleOrderResponse> orderResponses = SaleOrderConverter.toResponseList(orderDTOs);
        
        // 如果获取用户订单列表失败或者用户订单列表为空，则返回对应的错误响应；否则返回成功响应并携带用户订单列表
        return !ordersResult.isSuccess() || orderDTOs == null
                ? ResponseConverter.toMultiResponse(ordersResult)
                : MultiResponse.of(orderResponses, ordersResult.getTotal());
    }
    
    // 下单
    @PostMapping
    public SingleResponse<PlaceOrderResult> placeOrder(@RequestAttribute Long userId, @RequestBody PlaceOrderRequest placeOrderRequest) {
        // 将下单请求对象转换为下单命令对象
        PlaceOrderCommand placeOrderCommand = SaleOrderConverter.toCommand(placeOrderRequest);
        
        // 从应用层下单，并将下单结果转换为下单结果对象
        AppSingleResult<PlaceOrderResult> placeOrderResult = saleOrderAppService.placeOrder(userId, placeOrderCommand);
        
        // 判断下单结果是否成功，并且下单数据不为空，若成功则返回成功响应，否则返回对应的失败响应
        return !placeOrderResult.isSuccess() || placeOrderResult.getData() == null
                ? ResponseConverter.toSingleResponse(placeOrderResult)
                : SingleResponse.of(placeOrderResult.getData());
    }
    
    // 取消订单
    @PutMapping("/{orderId}")
    public Response cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        // 从应用层取消订单
        AppResult cancelResult = saleOrderAppService.cancelOrder(userId, orderId);
        // 返回取消订单结果的响应
        return ResponseConverter.toResponse(cancelResult);
    }
}
