package com.harris.app.service.app.impl;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.converter.PlaceOrderTaskConverter;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.enums.OrderTaskStatus;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.OrderTaskHandleResult;
import com.harris.app.model.result.OrderTaskSubmitResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.FlashActivityAppService;
import com.harris.app.service.app.FlashItemAppService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.PlaceOrderTaskService;
import com.harris.app.util.OrderNoService;
import com.harris.app.util.OrderTaskIdService;
import com.harris.domain.service.FlashItemDomainService;
import com.harris.domain.service.FlashOrderDomainService;
import com.harris.domain.service.StockDomainService;
import com.harris.infra.cache.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.harris.app.exception.AppErrCode.*;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class QueuedPlaceOrderService implements PlaceOrderService {
    private static final String PLACE_ORDER_TASK_ORDER_ID_KEY = "PLACE_ORDER_TASK_ORDER_ID_KEY_";

    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private FlashOrderDomainService flashOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private FlashActivityAppService flashActivityAppService;

    @Resource
    private FlashItemAppService flashItemAppService;

    @Resource
    private PlaceOrderTaskService placeOrderTaskService;

    @Resource
    private OrderTaskIdService orderTaskIdService;

    @Resource
    private OrderNoService orderNoService;

    @PostConstruct
    public void init() {
        log.info("init QueuedPlaceOrderService done");
    }

    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand) {
        if (placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            return PlaceOrderResult.error(INVALID_PARAMS);
        }

        // Get the flash item information
        AppSingleResult<FlashItemDTO> flashItemResult = flashItemAppService.getFlashItem(placeOrderCommand.getItemId());
        if (!flashItemResult.isSuccess() || flashItemResult.getData() == null) {
            log.info("doPlaceOrder, failed get the item: {},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.error(GET_ITEM_FAILED);
        }

        // Check if the flash item is on sale
        FlashItemDTO flashItemDTO = flashItemResult.getData();
        if (!flashItemDTO.isOnSale()) {
            log.info("doPlaceOrder, item not on sale: {},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.error(ITEM_NOT_ON_SALE);
        }

        // Generate the place order task ID and build the place order task
        String placeOrderTaskId = orderTaskIdService.generateOrderTaskId(userId, placeOrderCommand.getItemId());
        PlaceOrderTask placeOrderTask = PlaceOrderTaskConverter.with(userId, placeOrderCommand);
        placeOrderTask.setPlaceOrderTaskId(placeOrderTaskId);

        // Submit the place order task
        OrderTaskSubmitResult submitResult = placeOrderTaskService.submit(placeOrderTask);
        if (!submitResult.isSuccess()) {
            log.info("doPlaceOrder FAILED: {},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.error(submitResult.getCode(), submitResult.getMessage());
        }
        log.info("doPlaceOrder DONE: {},{}", userId, placeOrderTaskId);
        return PlaceOrderResult.ok(placeOrderTaskId);
    }

    public OrderTaskHandleResult getPlaceOrderResult(Long userId, Long itemId, String placeOrderTaskId) {
        String generatedPlaceOrderTaskId = orderTaskIdService.generateOrderTaskId(userId, itemId);
        if (!generatedPlaceOrderTaskId.equals(placeOrderTaskId)) {
            return OrderTaskHandleResult.error(PLACE_ORDER_TASK_ID_INVALID);
        }
        OrderTaskStatus orderTaskStatus = placeOrderTaskService.getTaskStatus(placeOrderTaskId);
        if (orderTaskStatus == null) {
            return OrderTaskHandleResult.error(PLACE_ORDER_TASK_ID_INVALID);
        }
        if (!OrderTaskStatus.SUCCESS.equals(orderTaskStatus)) {
            return OrderTaskHandleResult.error(orderTaskStatus);
        }
        Long orderId = redisCacheService.getObject(PLACE_ORDER_TASK_ORDER_ID_KEY + placeOrderTaskId, Long.class);
        return OrderTaskHandleResult.ok(orderId);
    }
}
