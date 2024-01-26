package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.command.PurchaseCommand;
import com.harris.app.model.converter.FssOrderAppConverter;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.security.SecurityService;
import com.harris.app.service.app.FssOrderAppService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.FssOrderDomainService;
import com.harris.domain.service.StockDomainService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssOrderAppServiceImpl implements FssOrderAppService {
    private static final String PLACE_ORDER_LOCK_KEY = "PURCHASE_LOCK_KEY";

    @Resource
    private SecurityService securityService;

    @Resource
    private FssOrderDomainService fssOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private PlaceOrderService placeOrderService;

    @Resource
    private StockCacheService stockCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    @Transactional
    public AppSingleResult<PurchaseResult> placeOrder(Long userId, PurchaseCommand purchaseCommand) {
        log.info("App placeOrder: {},{}", userId, purchaseCommand);
        if (userId == null || purchaseCommand == null || purchaseCommand.invalidParams()) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildPlaceOrderLockKey(userId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLocked = distributedLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return AppSingleResult.error(AppErrCode.LOCK_FAILED.getErrCode(), AppErrCode.LOCK_FAILED.getErrDesc());
            }

            // Check if the user has passed the risk inspection
            // For here, we just return true
            boolean isRisk = securityService.inspectRisksByPolicy(userId);
            if (!isRisk) {
                log.info("App placeOrder failed: {},{}", userId, purchaseCommand);
                return AppSingleResult.error(AppErrCode.PLACE_ORDER_FAILED);
            }

            // Place order
            PurchaseResult purchaseResult = placeOrderService.doPlaceOrder(userId, purchaseCommand);
            if (!purchaseResult.isSuccess()) {
                log.info("App placeOrder failed: {},{}", userId, purchaseResult);
                return AppSingleResult.error(purchaseResult.getCode(), purchaseResult.getMsg());
            }

            log.info("App placeOrder ok: {},{}", userId, purchaseResult);
            return AppSingleResult.ok(purchaseResult);
        } catch (Exception e) {
            log.error("App placeOrder failed: {},{}", userId, purchaseCommand, e);
            return AppSingleResult.error(AppErrCode.PLACE_ORDER_FAILED);
        } finally {
            // Release lock
            distributedLock.unlock();
        }
    }

    @Override
    public AppSingleResult<OrderHandleResult> getOrder(Long userId, Long itemId, String placeOrderTaskId) {
        log.info("App getOrder: {},{},{}", userId, itemId, placeOrderTaskId);
        if (userId == null || itemId == null || StringUtils.isEmpty(placeOrderTaskId)) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Check the type of place order service
        if (placeOrderService instanceof QueuedPlaceOrderService) {
            // Call the queued place order service to get the order handling result
            QueuedPlaceOrderService queuedPlaceOrderService = (QueuedPlaceOrderService) placeOrderService;
            OrderHandleResult orderHandleResult = queuedPlaceOrderService.getPlaceOrderResult(userId, itemId, placeOrderTaskId);
            if (!orderHandleResult.isSuccess()) {
                log.info("App getOrder failed: {},{},{}", userId, itemId, placeOrderTaskId);
                return AppSingleResult.error(orderHandleResult.getCode(), orderHandleResult.getMsg(), orderHandleResult);
            }

            log.info("App getOrder ok: {},{},{}", userId, itemId, placeOrderTaskId);
            return AppSingleResult.ok(orderHandleResult);
        } else {
            log.info("App getOrder failed: {},{},{}", userId, itemId, placeOrderTaskId);
            return AppSingleResult.error(AppErrCode.ORDER_TYPE_NOT_SUPPORT);
        }
    }

    @Override
    public AppMultiResult<SaleOrderDTO> listOrdersByUser(Long userId, SaleOrdersQuery saleOrdersQuery) {
        log.info("App listOrdersByUser: {},{}", userId, saleOrdersQuery);

        // Get orders and convert to DTOs
        PageQueryCondition condition = FssOrderAppConverter.toQuery(saleOrdersQuery);
        PageResult<SaleOrder> orderPageResult = fssOrderDomainService.getOrdersByUserId(userId, condition);
        List<SaleOrderDTO> saleOrderDTOS = orderPageResult
                .getData()
                .stream()
                .map(FssOrderAppConverter::toDTO)
                .collect(Collectors.toList());

        log.info("App listOrdersByUser ok: {},{}", userId, saleOrdersQuery);
        return AppMultiResult.of(saleOrderDTOS, orderPageResult.getTotal());
    }

    @Override
    @Transactional
    public AppResult cancelOrder(Long userId, Long orderId) {
        log.info("App cancelOrder: {},{}", userId, orderId);
        if (userId == null || orderId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Get order and validate
        SaleOrder saleOrder = fssOrderDomainService.getOrder(userId, orderId);
        if (saleOrder == null) {
            throw new BizException(AppErrCode.ORDER_NOT_FOUND);
        }

        // Cancel order
        boolean isCancelled = fssOrderDomainService.cancelOrder(userId, orderId);
        if (!isCancelled) {
            log.info("App cancelOrder failed: {},{}", userId, orderId);
            return AppResult.error(AppErrCode.ORDER_CANCEL_FAILED);
        }

        // Build stock deduction object
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(saleOrder.getItemId())
                .setQuantity(saleOrder.getQuantity())
                .setUserId(userId);

        // Reverse stock
        boolean isReverted = stockDomainService.revertStock(stockDeduction);
        if (!isReverted) {
            log.info("App cancelOrder revert failed: {},{}", userId, orderId);
            throw new BizException(AppErrCode.ORDER_CANCEL_FAILED);
        }

        // Reverse stock in cache
        boolean isCacheReverted = stockCacheService.revertStock(stockDeduction);
        if (!isCacheReverted) {
            log.info("App cancelOrder revert failed in cache: {},{}", userId, orderId);
            throw new BizException(AppErrCode.ORDER_CANCEL_FAILED);
        }

        log.info("App cancelOrder ok: {},{}", userId, orderId);
        return AppResult.ok();
    }

    private String buildPlaceOrderLockKey(Long userId) {
        return link(PLACE_ORDER_LOCK_KEY, userId);
    }
}
