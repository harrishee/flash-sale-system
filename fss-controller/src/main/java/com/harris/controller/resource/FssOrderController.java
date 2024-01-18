package com.harris.controller.resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.PurchaseCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.service.app.FssOrderAppService;
import com.harris.controller.model.converter.SaleOrderConverter;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.request.PurchaseRequest;
import com.harris.controller.model.response.SaleOrderResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
public class FssOrderController {
    @Resource
    private FssOrderAppService fssOrderAppService;

    @GetMapping(value = "/items/{itemId}/flash-orders/{orderId}")
    public SingleResponse<OrderHandleResult> getOrder(@RequestAttribute Long userId,
                                                      @PathVariable Long itemId,
                                                      @PathVariable String orderId) {
        AppSingleResult<OrderHandleResult> getResult = fssOrderAppService.getOrder(userId, itemId, orderId);
        OrderHandleResult orderHandleResult = getResult.getData();

        return !getResult.isSuccess() || orderHandleResult == null
                ? ResponseConverter.toSingleResponse(getResult)
                : SingleResponse.of(orderHandleResult);
    }

    @GetMapping(value = "/flash-orders/my")
    public MultiResponse<SaleOrderResponse> listOrders(@RequestAttribute Long userId,
                                                       @RequestParam Integer pageSize,
                                                       @RequestParam Integer pageNumber,
                                                       @RequestParam(required = false) String keyword) {
        SaleOrdersQuery saleOrdersQuery = new SaleOrdersQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<SaleOrderDTO> listResult = fssOrderAppService.listOrdersByUser(userId, saleOrdersQuery);
        Collection<SaleOrderDTO> saleOrderDTOs = listResult.getData();
        Collection<SaleOrderResponse> saleOrderResponses = SaleOrderConverter.toResponseList(saleOrderDTOs);

        return !listResult.isSuccess() || saleOrderDTOs == null
                ? ResponseConverter.toMultiResponse(listResult)
                : MultiResponse.of(saleOrderResponses, listResult.getTotal());
    }

    @PostMapping(value = "/flash-orders")
    public SingleResponse<PurchaseResult> Purchase(@RequestAttribute Long userId,
                                                   @RequestBody PurchaseRequest purchaseRequest) {
        PurchaseCommand purchaseCommand = SaleOrderConverter.toCommand(purchaseRequest);
        AppSingleResult<PurchaseResult> purchaseResult = fssOrderAppService.placeOrder(userId, purchaseCommand);

        return !purchaseResult.isSuccess() || purchaseResult.getData() == null
                ? ResponseConverter.toSingleResponse(purchaseResult)
                : SingleResponse.of(purchaseResult.getData());
    }

    @PutMapping(value = "/flash-orders/{orderId}/cancel")
    public Response cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        AppResult cancelResult = fssOrderAppService.cancelOrder(userId, orderId);
        return ResponseConverter.toResponse(cancelResult);
    }
}
