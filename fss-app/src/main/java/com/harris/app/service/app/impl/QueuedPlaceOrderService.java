package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.converter.PlaceOrderTaskConverter;
import com.harris.app.model.converter.SaleOrderAppConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.enums.PlaceOrderTaskStatus;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.OrderHandleResult;
import com.harris.app.model.result.OrderSubmitResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.PlaceOrderTaskService;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.app.service.app.SaleItemAppService;
import com.harris.app.util.OrderUtil;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.SaleItemDomainService;
import com.harris.domain.service.SaleOrderDomainService;
import com.harris.domain.service.StockDomainService;
import com.harris.infra.cache.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class QueuedPlaceOrderService implements PlaceOrderService {
    private static final String PL_TASK_ORDER_ID_KEY = "PLACE_ORDER_TASK_ORDER_ID_KEY_";

    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private SaleItemDomainService saleItemDomainService;

    @Resource
    private SaleOrderDomainService saleOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private SaleActivityAppService saleActivityAppService;

    @Resource
    private SaleItemAppService saleItemAppService;

    @Resource
    private PlaceOrderTaskService placeOrderTaskService;

    @PostConstruct
    public void init() {
        log.info("QueuedPlaceOrderService initialized");
    }

    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        log.info("Queued placeOrder, start: {},{}", userId, JSON.toJSONString(placeOrderCommand));
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            return PlaceOrderResult.error(AppErrorCode.INVALID_PARAMS);
        }

        // Get the item info and validate
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(placeOrderCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("Queued placeOrder, get item failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(AppErrorCode.GET_ITEM_FAILED);
        }

        // Check if the item is on sale
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (saleItemDTO.notOnSale()) {
            log.info("Queued placeOrder, item not on sale: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(AppErrorCode.ITEM_NOT_ON_SALE);
        }

        // Generate the place order task ID and build the place order task
        String placeOrderTaskId = OrderUtil.generateOrderTaskId(userId, placeOrderCommand.getItemId());
        PlaceOrderTask placeOrderTask = PlaceOrderTaskConverter.toTask(userId, placeOrderCommand);
        placeOrderTask.setPlaceOrderTaskId(placeOrderTaskId);

        // Submit the place order task
        OrderSubmitResult submitResult = placeOrderTaskService.submit(placeOrderTask);
        if (!submitResult.isSuccess()) {
            log.info("Queued placeOrder, submit task failed: {},{}", userId, JSON.toJSONString(placeOrderCommand));
            return PlaceOrderResult.error(submitResult.getCode(), submitResult.getMessage());
        }

        log.info("Queued placeOrder, submit task success: {},{}", userId, JSON.toJSONString(placeOrderCommand));
        return PlaceOrderResult.ok(placeOrderTaskId);
    }

    public OrderHandleResult getPlaceOrderResult(Long userId, Long itemId, String placeOrderTaskId) {
        // Generate the task ID based on user and item IDs and validate with the provided task ID
        String expectedTaskId = OrderUtil.generateOrderTaskId(userId, itemId);
        if (!expectedTaskId.equals(placeOrderTaskId)) {
            return OrderHandleResult.error(AppErrorCode.PLACE_ORDER_TASK_ID_INVALID);
        }

        // Retrieve the status of the place order task
        PlaceOrderTaskStatus placeOrderTaskStatus = placeOrderTaskService.getStatus(placeOrderTaskId);
        if (placeOrderTaskStatus == null) {
            return OrderHandleResult.error(AppErrorCode.PLACE_ORDER_TASK_ID_INVALID);
        }

        // Check if the order task was successful
        if (!PlaceOrderTaskStatus.SUCCESS.equals(placeOrderTaskStatus)) {
            return OrderHandleResult.error(placeOrderTaskStatus);
        }

        // Retrieve the order ID associated with the task
        Long orderId = redisCacheService.getObject(PL_TASK_ORDER_ID_KEY + placeOrderTaskId, Long.class);
        return OrderHandleResult.ok(orderId);
    }

    @Transactional
    public void handlePlaceOrderTask(PlaceOrderTask placeOrderTask) {
        try {
            Long userId = placeOrderTask.getUserId();

            // Check if the sale activity allows placing order
            boolean activityAllowed = saleActivityAppService.isPlaceOrderAllowed(placeOrderTask.getActivityId());
            if (!activityAllowed) {
                log.info("Queued placeOrder, activity rules failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getActivityId());
                placeOrderTaskService.updateHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }

            // Check if the sale item allows placing order
            boolean itemAllowed = saleItemAppService.isPlaceOrderAllowed(placeOrderTask.getItemId());
            if (!itemAllowed) {
                log.info("Queued placeOrder, item rules failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getItemId());
                placeOrderTaskService.updateHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }

            // Retrieve sale item details
            SaleItem saleItem = saleItemDomainService.getItem(placeOrderTask.getItemId());

            // Generate the order ID
            Long orderId = OrderUtil.generateOrderNo();
            SaleOrder saleOrderToPlace = SaleOrderAppConverter.toDomainModel(placeOrderTask);

            // Build the new order object
            saleOrderToPlace.setItemTitle(saleItem.getItemTitle());
            saleOrderToPlace.setSalePrice(saleItem.getSalePrice());
            saleOrderToPlace.setUserId(userId);
            saleOrderToPlace.setId(orderId);

            // Build the stock deduction object
            StockDeduction stockDeduction = new StockDeduction()
                    .setItemId(placeOrderTask.getItemId())
                    .setQuantity(placeOrderTask.getQuantity());

            // Deduct stock from DB
            boolean deductSuccess = stockDomainService.deductStock(stockDeduction);
            if (!deductSuccess) {
                log.info("Queued placeOrder, deduct stock failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
                return;
            }

            // Place the order
            boolean placeOrderSuccess = saleOrderDomainService.placeOrder(userId, saleOrderToPlace);
            if (!placeOrderSuccess) {
                log.info("Queued placeOrder, place order failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
                throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }

            // Update the task status as successful
            placeOrderTaskService.updateHandleResult(placeOrderTask.getPlaceOrderTaskId(), true);

            // Cache the order ID associated with the task
            redisCacheService.put(PL_TASK_ORDER_ID_KEY + placeOrderTask.getPlaceOrderTaskId(),
                    orderId, CacheConstant.HOURS_24);
            log.info("Queued placeOrder, place order success: {},{}",
                    placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
        } catch (Exception e) {
            // Update the task status as failed in case of exceptions
            placeOrderTaskService.updateHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
            log.error("Queued placeOrder, place order failed: {},{}",
                    placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask), e);
            throw new BizException(e.getMessage());
        }
    }
}
