package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.exception.BizException;
import com.harris.app.model.OrderNoContext;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.converter.FlashOrderAppConverter;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.FlashActivityAppService;
import com.harris.app.service.app.FlashItemAppService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.cache.ItemStockCacheService;
import com.harris.app.util.OrderNoService;
import com.harris.app.util.PlaceOrderTypeCondition;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.FlashOrder;
import com.harris.domain.service.FlashOrderDomainService;
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
    private FlashOrderDomainService flashOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private FlashActivityAppService flashActivityAppService;

    @Resource
    private FlashItemAppService flashItemAppService;

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Resource
    private OrderNoService orderNoService;

    @PostConstruct
    public void init() {
        log.info("init StandardPlaceOrderService done");
    }

    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand) {
        log.info("placeOrder, start: {},{}", userId, JSON.toJSONString(placeOrderCommand));
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            throw new BizException(INVALID_PARAMS);
        }

        // Check if the flash activity allows placing order
        boolean isAllowed = flashActivityAppService.isPlaceOrderAllowed(placeOrderCommand.getActivityId());
        if (!isAllowed) {
            log.info("placeOrder, activity not allowed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(PLACE_ORDER_FAILED);
        }

        // Check if the flash item allows placing order
        boolean isItemAllowPlaceOrder = flashItemAppService.isPlaceOrderAllowed(placeOrderCommand.getItemId());
        if (!isItemAllowPlaceOrder) {
            log.info("placeOrder, item not allowed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(PLACE_ORDER_FAILED);
        }

        // Get the flash item information
        AppSingleResult<FlashItemDTO> flashItemResult = flashItemAppService.getFlashItem(placeOrderCommand.getItemId());
        if (!flashItemResult.isSuccess() || flashItemResult.getData() == null) {
            return PlaceOrderResult.error(ITEM_NOT_FOUND);
        }
        FlashItemDTO flashItem = flashItemResult.getData();

        // Generate the order ID and build the order
        Long orderId = orderNoService.generateOrderNo(new OrderNoContext());
        FlashOrder flashOrderToPlace = FlashOrderAppConverter.toDomainObj(placeOrderCommand);
        flashOrderToPlace.setItemTitle(flashItem.getItemTitle());
        flashOrderToPlace.setFlashPrice(flashItem.getFlashPrice());
        flashOrderToPlace.setUserId(userId);
        flashOrderToPlace.setId(orderId);

        // Build the stock deduction object
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(placeOrderCommand.getItemId())
                .setQuantity(placeOrderCommand.getQuantity())
                .setUserId(userId);

        boolean predeductResult = false;
        try {
            // Pre-deduct the stock
            predeductResult = itemStockCacheService.deductItemStock(stockDeduction);
            if (!predeductResult) {
                log.info("placeOrder, pre deduct failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
                return PlaceOrderResult.error(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }

            // Actually deduct the stock
            boolean decreaseStockSuccess = stockDomainService.decreaseItemStock(stockDeduction);
            if (!decreaseStockSuccess) {
                log.info("placeOrder, deduct failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
                return PlaceOrderResult.error(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }

            // Place the order
            boolean placeOrderResult = flashOrderDomainService.placeOrder(userId, flashOrderToPlace);
            if (!placeOrderResult) {
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }
        } catch (Exception e) {
            // If failed to place the order, recover the pre-deduct stock
            if (predeductResult) {
                boolean recoverStockSuccess = itemStockCacheService.increaseItemStock(stockDeduction);
                if (!recoverStockSuccess) {
                    log.error("placeOrder, recover the pre-deduct failed: {},{}", userId, JSON.toJSONString(placeOrderCommand), e);
                }
            }
            log.error("placeOrder failed: {},{}", userId, JSON.toJSONString(placeOrderCommand), e);
            throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
        }
        log.info("placeOrder DONE: {},{}", userId, orderId);
        return PlaceOrderResult.ok(orderId);
    }
}
