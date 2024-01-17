package com.harris.controller.resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.service.app.FlashOrderAppService;
import com.harris.controller.model.converter.FlashOrderConverter;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.request.PlaceOrderRequest;
import com.harris.controller.model.response.FlashOrderResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class FlashOrderController {
    @Resource
    private FlashOrderAppService flashOrderAppService;

    @GetMapping(value = "/items/{itemId}/flash-orders/{placeOrderTaskId}")
    public SingleResponse<OrderTaskHandleResult> getPlaceOrderTaskResult(@RequestAttribute Long userId, @PathVariable Long itemId, @PathVariable String placeOrderTaskId) {
        AppSingleResult<OrderTaskHandleResult> placeOrderTaskResult = flashOrderAppService.getPlaceOrderTaskResult(userId, itemId, placeOrderTaskId);
        if (!placeOrderTaskResult.isSuccess() || placeOrderTaskResult.getData() == null) {
            return ResponseConverter.withSingle(placeOrderTaskResult);
        }
        return SingleResponse.of(placeOrderTaskResult.getData());
    }

    @GetMapping(value = "/flash-orders/my")
    public MultiResponse<FlashOrderResponse> myOrders(@RequestAttribute Long userId,
                                                      @RequestParam Integer pageSize,
                                                      @RequestParam Integer pageNumber,
                                                      @RequestParam(required = false) String keyword) {
        FlashOrdersQuery flashOrdersQuery = new FlashOrdersQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<FlashOrderDTO> flashOrdersResult = flashOrderAppService.getOrdersByUser(userId, flashOrdersQuery);
        if (!flashOrdersResult.isSuccess() || flashOrdersResult.getData() == null) {
            return ResponseConverter.withMulti(flashOrdersResult);
        }
        return MultiResponse.of(FlashOrderConverter.toResponses(flashOrdersResult.getData()), flashOrdersResult.getTotal());
    }

    @PostMapping(value = "/flash-orders")
    public SingleResponse<PlaceOrderResult> placeOrder(@RequestAttribute Long userId, @RequestBody PlaceOrderRequest placeOrderRequest) {
        FlashPlaceOrderCommand placeOrderCommand = FlashOrderConverter.toCommand(placeOrderRequest);
        AppSingleResult<PlaceOrderResult> placeOrderResult = flashOrderAppService.placeOrder(userId, placeOrderCommand);
        if (!placeOrderResult.isSuccess() || placeOrderResult.getData() == null) {
            return ResponseConverter.withSingle(placeOrderResult);
        }
        return SingleResponse.of(placeOrderResult.getData());
    }

    @PutMapping(value = "/flash-orders/{orderId}/cancel")
    public Response cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        AppResult appResult = flashOrderAppService.cancelOrder(userId, orderId);
        return ResponseConverter.with(appResult);
    }
}
