package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.converter.SaleOrderAppConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.app.service.app.SaleItemAppService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.app.util.OrderUtil;
import com.harris.app.util.PlaceOrderCondition;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.SaleOrderDomainService;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Service
@Conditional(PlaceOrderCondition.class)
public class StandardPlaceOrderService implements PlaceOrderService {
    @Resource
    private SaleOrderDomainService saleOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private SaleActivityAppService saleActivityAppService;

    @Resource
    private SaleItemAppService saleItemAppService;

    @Resource
    private StockCacheService stockCacheService;

    @PostConstruct
    public void init() {
        log.info("StandardPlaceOrderService initialized");
    }

    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        log.info("Standard placeOrder, start: {},{}", userId, JSON.toJSONString(placeOrderCommand));
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Check if the sale activity allows placing order
        boolean activityAllowed = saleActivityAppService.isPlaceOrderAllowed(placeOrderCommand.getActivityId());
        if (!activityAllowed) {
            log.info("Standard placeOrder, activity not allowed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        }

        // Check if the sale item allows placing order
        boolean itemAllowed = saleItemAppService.isPlaceOrderAllowed(placeOrderCommand.getItemId());
        if (!itemAllowed) {
            log.info("Standard placeOrder, item not allowed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        }

        // Get the item info and validate
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(placeOrderCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("Standard placeOrder, get item failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(AppErrorCode.GET_ITEM_FAILED);
        }

        // Check if the item is on sale
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (saleItemDTO.notOnSale()) {
            log.info("Queued placeOrder, item not on sale: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(AppErrorCode.ITEM_NOT_ON_SALE);
        }

        // Generate the order ID
        Long orderId = OrderUtil.generateOrderNo();
        SaleOrder newOrder = SaleOrderAppConverter.toDomainModel(placeOrderCommand);

        // Build the new order object
        newOrder.setItemTitle(saleItemDTO.getItemTitle());
        newOrder.setSalePrice(saleItemDTO.getSalePrice());
        newOrder.setUserId(userId);
        newOrder.setId(orderId);

        // Build the stock deduction object
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(placeOrderCommand.getItemId())
                .setQuantity(placeOrderCommand.getQuantity())
                .setUserId(userId);

        boolean preDeductSuccess = false;

        try {
            // 1. Pre-deduct the stock from cache
            preDeductSuccess = stockCacheService.deductStock(stockDeduction);
            if (!preDeductSuccess) {
                log.info("Standard placeOrder, pre-deduct failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
                return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED.getErrCode(),
                        AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }

            // 2. Deduct the stock from DB
            boolean deductSuccess = stockDomainService.deductStock(stockDeduction);
            if (!deductSuccess) {
                log.info("Standard placeOrder, deduct failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
                return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED.getErrCode(),
                        AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }

            // 3. Place the order
            boolean placeOrderSuccess = saleOrderDomainService.placeOrder(userId, newOrder);
            if (!placeOrderSuccess) {
                throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
        } catch (Exception e) {
            // Pre-deduct succeeded, but deduct or place order failed
            // So we need to revert the stock from cache
            if (preDeductSuccess) {
                boolean revertSuccess = stockCacheService.revertStock(stockDeduction);
                if (!revertSuccess) {
                    log.error("Standard placeOrder, revert failed: {},{}", userId,
                            JSON.toJSONString(placeOrderCommand), e);
                }
            }

            log.error("Standard placeOrder, place order failed: {},{}", userId,
                    JSON.toJSONString(placeOrderCommand), e);
            throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
        }

        log.info("Standard placeOrder, place order success: {},{}", userId, orderId);
        return PlaceOrderResult.ok(orderId);
    }
}
