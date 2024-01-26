package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.exception.BizException;
import com.harris.app.model.OrderNoContext;
import com.harris.app.model.command.PurchaseCommand;
import com.harris.app.model.converter.FssOrderAppConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.PurchaseResult;
import com.harris.app.service.app.FssActivityAppService;
import com.harris.app.service.app.FssItemAppService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.app.util.OrderNoService;
import com.harris.app.util.PlaceOrderTypeCondition;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.FssOrderDomainService;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.harris.app.exception.AppErrCode.*;

@Slf4j
@Service
@Conditional(PlaceOrderTypeCondition.class)
public class StandardPlaceOrderService implements PlaceOrderService {
    @Resource
    private FssOrderDomainService fssOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private FssActivityAppService fssActivityAppService;

    @Resource
    private FssItemAppService fssItemAppService;

    @Resource
    private StockCacheService stockCacheService;

    @Resource
    private OrderNoService orderNoService;

    @PostConstruct
    public void init() {
        log.info("StandardPlaceOrderService initialized");
    }

    @Override
    public PurchaseResult doPlaceOrder(Long userId, PurchaseCommand purchaseCommand) {
        log.info("Standard placeOrder, start: {},{}", userId, JSON.toJSONString(purchaseCommand));

        // Validate params
        if (userId == null || purchaseCommand == null || purchaseCommand.invalidParams()) {
            throw new BizException(INVALID_PARAMS);
        }

        // Check if the sale activity allows placing order
        boolean isActivityAllowed = fssActivityAppService.isPlaceOrderAllowed(purchaseCommand.getActivityId());
        if (!isActivityAllowed) {
            log.info("Standard placeOrder, activity not allowed: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(PLACE_ORDER_FAILED);
        }

        // Check if the sale item allows placing order
        boolean isItemAllowed = fssItemAppService.isPlaceOrderAllowed(purchaseCommand.getItemId());
        if (!isItemAllowed) {
            log.info("Standard placeOrder, item not allowed: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(PLACE_ORDER_FAILED);
        }

        // Get the item info and validate
        AppSingleResult<SaleItemDTO> itemResult = fssItemAppService.getItem(purchaseCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("Standard placeOrder, get item failed: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(GET_ITEM_FAILED);
        }

        // Check if the item is on sale
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (!saleItemDTO.isOnSale()) {
            log.info("Queued placeOrder, item not on sale: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(ITEM_NOT_ON_SALE);
        }

        // Generate the order ID
        Long orderId = orderNoService.generateOrderNo(new OrderNoContext());
        SaleOrder newOrder = FssOrderAppConverter.toDomainModel(purchaseCommand);

        // Build the new order object
        newOrder.setItemTitle(saleItemDTO.getItemTitle());
        newOrder.setSalePrice(saleItemDTO.getSalePrice());
        newOrder.setUserId(userId);
        newOrder.setId(orderId);

        // Build the stock deduction object
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(purchaseCommand.getItemId())
                .setQuantity(purchaseCommand.getQuantity())
                .setUserId(userId);

        boolean preDeductResult = false;
        try {
            // 1. Pre-deduct the stock from cache
            preDeductResult = stockCacheService.deductStock(stockDeduction);
            if (!preDeductResult) {
                log.info("Standard placeOrder, pre-deduct failed: {},{}", userId, JSON.toJSONString(purchaseCommand));
                return PurchaseResult.error(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }

            // 2. Deduct the stock from DB
            boolean deductResult = stockDomainService.deductStock(stockDeduction);
            if (!deductResult) {
                log.info("Standard placeOrder, deduct failed: {},{}", userId, JSON.toJSONString(purchaseCommand));
                return PurchaseResult.error(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }

            // 3. Place the order
            boolean placeOrderResult = fssOrderDomainService.placeOrder(userId, newOrder);
            if (!placeOrderResult) {
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }
        } catch (Exception e) {
            // Pre-deduct succeeded, but deduct or place order failed
            // So we need to revert the stock from cache
            if (preDeductResult) {
                boolean revertResult = stockCacheService.revertStock(stockDeduction);
                if (!revertResult) {
                    log.error("Standard placeOrder, revert failed: {},{}", userId, JSON.toJSONString(purchaseCommand), e);
                }
            }
            log.error("Standard placeOrder, place order failed: {},{}", userId, JSON.toJSONString(purchaseCommand), e);
            throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
        }
        log.info("Standard placeOrder, place order success: {},{}", userId, orderId);
        return PurchaseResult.ok(orderId);
    }
}
