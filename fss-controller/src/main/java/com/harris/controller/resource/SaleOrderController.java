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

    @GetMapping("/{orderId}")
    public SingleResponse<OrderHandleResult> getOrder(@RequestAttribute Long userId,
                                                      @PathVariable Long itemId,
                                                      @PathVariable String orderId) {

        AppSingleResult<OrderHandleResult> orderResult = saleOrderAppService.getOrder(userId, itemId, orderId);

        OrderHandleResult orderHandleResult = orderResult.getData();

        return !orderResult.isSuccess() || orderHandleResult == null
                ? ResponseConverter.toSingleResponse(orderResult)
                : SingleResponse.of(orderHandleResult);
    }

    @GetMapping("/user")
    public MultiResponse<SaleOrderResponse> listUserOrders(@RequestAttribute Long userId,
                                                           @RequestParam Integer pageSize,
                                                           @RequestParam Integer pageNumber,
                                                           @RequestParam(required = false) String keyword) {

        SaleOrdersQuery ordersQuery = new SaleOrdersQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);

        AppMultiResult<SaleOrderDTO> ordersResult = saleOrderAppService.listOrdersByUser(userId, ordersQuery);

        Collection<SaleOrderDTO> orderDTOs = ordersResult.getData();
        Collection<SaleOrderResponse> orderResponses = SaleOrderConverter.toResponseList(orderDTOs);

        return !ordersResult.isSuccess() || orderDTOs == null
                ? ResponseConverter.toMultiResponse(ordersResult)
                : MultiResponse.of(orderResponses, ordersResult.getTotal());
    }

    @PostMapping
    public SingleResponse<PlaceOrderResult> placeOrder(@RequestAttribute Long userId,
                                                       @RequestBody PlaceOrderRequest placeOrderRequest) {

        PlaceOrderCommand placeOrderCommand = SaleOrderConverter.toCommand(placeOrderRequest);
        AppSingleResult<PlaceOrderResult> placeOrderResult = saleOrderAppService.placeOrder(userId, placeOrderCommand);

        return !placeOrderResult.isSuccess() || placeOrderResult.getData() == null
                ? ResponseConverter.toSingleResponse(placeOrderResult)
                : SingleResponse.of(placeOrderResult.getData());
    }

    @PutMapping("/{orderId}")
    public Response cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        AppResult cancelResult = saleOrderAppService.cancelOrder(userId, orderId);

        return ResponseConverter.toResponse(cancelResult);
    }
}
