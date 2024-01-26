package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.exception.BizException;
import com.harris.app.model.OrderNoContext;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PurchaseCommand;
import com.harris.app.model.converter.FssOrderAppConverter;
import com.harris.app.model.converter.PlaceOrderTaskConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.enums.OrderTaskStatus;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.OrderHandleResult;
import com.harris.app.model.result.OrderTaskSubmitResult;
import com.harris.app.model.result.PurchaseResult;
import com.harris.app.service.app.FssActivityAppService;
import com.harris.app.service.app.FssItemAppService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.PlaceOrderTaskService;
import com.harris.app.util.OrderNoService;
import com.harris.app.util.OrderTaskIdService;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.FssItemDomainService;
import com.harris.domain.service.FssOrderDomainService;
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
    private static final String PL_TASK_ORDER_ID_KEY = "PLACE_ORDER_TASK_ORDER_ID_KEY_";

    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private FssItemDomainService fssItemDomainService;

    @Resource
    private FssOrderDomainService fssOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private FssActivityAppService fssActivityAppService;

    @Resource
    private FssItemAppService fssItemAppService;

    @Resource
    private PlaceOrderTaskService placeOrderTaskService;

    @Resource
    private OrderTaskIdService orderTaskIdService;

    @Resource
    private OrderNoService orderNoService;

    @PostConstruct
    public void init() {
        log.info("QueuedPlaceOrderService initialized");
    }

    @Override
    public PurchaseResult doPlaceOrder(Long userId, PurchaseCommand purchaseCommand) {
        log.info("Queued placeOrder, start: {},{}", userId, JSON.toJSONString(purchaseCommand));

        // Validate params
        if (userId == null || purchaseCommand == null || purchaseCommand.invalidParams()) {
            return PurchaseResult.error(INVALID_PARAMS);
        }

        // Get the item info and validate
        AppSingleResult<SaleItemDTO> itemResult = fssItemAppService.getItem(purchaseCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("Queued placeOrder, get item failed: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(GET_ITEM_FAILED);
        }

        // Check if the item is on sale
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (!saleItemDTO.isOnSale()) {
            log.info("Queued placeOrder, item not on sale: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(ITEM_NOT_ON_SALE);
        }

        // Generate the place order task ID and build the place order task
        String placeOrderTaskId = orderTaskIdService.generateOrderTaskId(userId, purchaseCommand.getItemId());
        PlaceOrderTask placeOrderTask = PlaceOrderTaskConverter.with(userId, purchaseCommand);
        placeOrderTask.setPlaceOrderTaskId(placeOrderTaskId);

        // Submit the place order task
        OrderTaskSubmitResult submitResult = placeOrderTaskService.submit(placeOrderTask);
        if (!submitResult.isSuccess()) {
            log.info("Queued placeOrder, submit task failed: {},{}", userId, JSON.toJSONString(purchaseCommand));
            return PurchaseResult.error(submitResult.getCode(), submitResult.getMsg());
        }
        log.info("Queued placeOrder, submit task success: {},{}", userId, JSON.toJSONString(purchaseCommand));
        return PurchaseResult.ok(placeOrderTaskId);
    }

    public OrderHandleResult getPlaceOrderResult(Long userId, Long itemId, String placeOrderTaskId) {
        // Generate the task ID based on user and item IDs and validate with the provided task ID
        String expectedTaskId = orderTaskIdService.generateOrderTaskId(userId, itemId);
        if (!expectedTaskId.equals(placeOrderTaskId)) {
            return OrderHandleResult.error(PLACE_ORDER_TASK_ID_INVALID);
        }

        // Retrieve the status of the place order task
        OrderTaskStatus orderTaskStatus = placeOrderTaskService.getTaskStatus(placeOrderTaskId);
        if (orderTaskStatus == null) {
            return OrderHandleResult.error(PLACE_ORDER_TASK_ID_INVALID);
        }

        // Check if the order task was successful
        if (!OrderTaskStatus.SUCCESS.equals(orderTaskStatus)) {
            return OrderHandleResult.error(orderTaskStatus);
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
            boolean isActivityAllowed = fssActivityAppService.isPlaceOrderAllowed(placeOrderTask.getActivityId());
            if (!isActivityAllowed) {
                log.info("Queued placeOrder, activity rules failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getActivityId());
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }

            // Check if the sale item allows placing order
            boolean isItemAllowed = fssItemAppService.isPlaceOrderAllowed(placeOrderTask.getItemId());
            if (!isItemAllowed) {
                log.info("Queued placeOrder, item rules failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getItemId());
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }

            // Retrieve sale item details
            SaleItem saleItem = fssItemDomainService.getItem(placeOrderTask.getItemId());

            // Generate the order ID
            Long orderId = orderNoService.generateOrderNo(new OrderNoContext());
            SaleOrder saleOrderToPlace = FssOrderAppConverter.toDomainModel(placeOrderTask);

            // Build the new order object
            saleOrderToPlace.setItemTitle(saleItem.getItemTitle());
            saleOrderToPlace.setSalePrice(saleItem.getFlashPrice());
            saleOrderToPlace.setUserId(userId);
            saleOrderToPlace.setId(orderId);

            // Build the stock deduction object
            StockDeduction stockDeduction = new StockDeduction()
                    .setItemId(placeOrderTask.getItemId())
                    .setQuantity(placeOrderTask.getQuantity());

            // Deduct stock from DB
            boolean deductResult = stockDomainService.deductStock(stockDeduction);
            if (!deductResult) {
                log.info("Queued placeOrder, deduct stock failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
                return;
            }

            // Place the order
            boolean placeOrderSuccess = fssOrderDomainService.placeOrder(userId, saleOrderToPlace);
            if (!placeOrderSuccess) {
                log.info("Queued placeOrder, place order failed: {},{}",
                        placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }

            // Update the task status as successful
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), true);

            // Cache the order ID associated with the task
            redisCacheService.put(PL_TASK_ORDER_ID_KEY + placeOrderTask.getPlaceOrderTaskId(), orderId, HOURS_24);
            log.info("Queued placeOrder, place order success: {},{}",
                    placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
        } catch (Exception e) {
            // Update the task status as failed in case of exceptions
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
            log.error("Queued placeOrder, place order failed: {},{}",
                    placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask), e);
            throw new BizException(e.getMessage());
        }
    }
}
