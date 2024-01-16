package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.exception.BizException;
import com.harris.app.model.OrderNoContext;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.converter.FlashOrderAppConverter;
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
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.model.entity.FlashOrder;
import com.harris.domain.service.FlashItemDomainService;
import com.harris.domain.service.FlashOrderDomainService;
import com.harris.domain.service.StockDomainService;
import com.harris.infra.cache.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.harris.app.exception.AppErrCode.*;
import static com.harris.app.model.cache.CacheConstant.HOURS_24;

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

    @Transactional
    public void handlePlaceOrderTask(PlaceOrderTask placeOrderTask) {
        try {
            // Check if placing order allowed
            Long userId = placeOrderTask.getUserId();
            boolean isActivityAllowed = flashActivityAppService.isPlaceOrderAllowed(placeOrderTask.getActivityId());
            if (!isActivityAllowed) {
                log.info("handlePlaceOrderTask, activity rules failed: {},{}", placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getActivityId());
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            boolean isItemAllowed = flashItemAppService.isPlaceOrderAllowed(placeOrderTask.getItemId());
            if (!isItemAllowed) {
                log.info("handlePlaceOrderTask, item rules failed: {},{}", placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getActivityId());
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }

            // Get the flash item information
            FlashItem flashItem = flashItemDomainService.getItem(placeOrderTask.getItemId());

            // Generate the order ID and build the flash order object
            Long orderId = orderNoService.generateOrderNo(new OrderNoContext());
            FlashOrder flashOrderToPlace = FlashOrderAppConverter.toDomainObj(placeOrderTask);
            flashOrderToPlace.setItemTitle(flashItem.getItemTitle());
            flashOrderToPlace.setFlashPrice(flashItem.getFlashPrice());
            flashOrderToPlace.setUserId(userId);
            flashOrderToPlace.setId(orderId);

            // Build the stock deduction object
            StockDeduction stockDeduction = new StockDeduction()
                    .setItemId(placeOrderTask.getItemId())
                    .setQuantity(placeOrderTask.getQuantity());

            // Deduct the stock
            boolean decreaseStockSuccess = stockDomainService.decreaseItemStock(stockDeduction);
            if (!decreaseStockSuccess) {
                log.info("handlePlaceOrderTask, stock deduction failed: {},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
                return;
            }

            // Place the order
            boolean placeOrderSuccess = flashOrderDomainService.placeOrder(userId, flashOrderToPlace);
            if (!placeOrderSuccess) {
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }

            // Update the task handle result
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), true);

            // Put the order ID into cache with a 24-hour expiration
            redisCacheService.put(PLACE_ORDER_TASK_ORDER_ID_KEY + placeOrderTask.getPlaceOrderTaskId(), orderId, HOURS_24);
            log.info("handlePlaceOrderTask, place order task success: {},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
        } catch (Exception e) {
            // Update the task handle result to false
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
            log.error("handlePlaceOrderTask, place order task failed: {},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask), e);
            throw new BizException(e.getMessage());
        }
    }
}
