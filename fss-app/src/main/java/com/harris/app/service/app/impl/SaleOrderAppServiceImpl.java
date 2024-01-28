package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.converter.SaleOrderAppConverter;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.service.security.SecurityService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.SaleOrderAppService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.SaleOrderDomainService;
import com.harris.domain.service.StockDomainService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import com.harris.infra.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaleOrderAppServiceImpl implements SaleOrderAppService {
    private static final String PLACE_ORDER_LOCK_KEY = "PLACE_ORDER_LOCK_KEY";

    @Resource
    private SecurityService securityService;

    @Resource
    private SaleOrderDomainService saleOrderDomainService;

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
    public AppSingleResult<PlaceOrderResult> placeOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        log.info("App placeOrder: {},{}", userId, placeOrderCommand);
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildPlaceOrderLockKey(userId));
        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return AppSingleResult.error(AppErrorCode.LOCK_FAILED.getErrCode(),
                        AppErrorCode.LOCK_FAILED.getErrDesc());
            }

            // Check if the user has passed the risk inspection
            // For here, we just return true for demo
            boolean notRisk = securityService.inspectRisksByPolicy(userId);
            if (!notRisk) {
                log.info("App placeOrder failed: {},{}", userId, placeOrderCommand);
                return AppSingleResult.error(AppErrorCode.PLACE_ORDER_FAILED);
            }

            // Place order
            PlaceOrderResult placeOrderResult = placeOrderService.doPlaceOrder(userId, placeOrderCommand);
            if (!placeOrderResult.isSuccess()) {
                log.info("App placeOrder failed: {},{}", userId, placeOrderResult);
                return AppSingleResult.error(placeOrderResult.getCode(), placeOrderResult.getMessage());
            }

            log.info("App placeOrder ok: {},{}", userId, placeOrderResult);
            return AppSingleResult.ok(placeOrderResult);
        } catch (Exception e) {
            log.error("App placeOrder failed: {},{}", userId, placeOrderCommand, e);
            return AppSingleResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public AppSingleResult<OrderHandleResult> getOrder(Long userId, Long itemId, String placeOrderTaskId) {
        log.info("App getOrder: {},{},{}", userId, itemId, placeOrderTaskId);
        if (userId == null || itemId == null || StringUtils.isEmpty(placeOrderTaskId)) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Check the type of place order service
        if (placeOrderService instanceof QueuedPlaceOrderService) {
            // Call the queued place order service to get the order handling result
            QueuedPlaceOrderService queuedPlaceOrderService = (QueuedPlaceOrderService) placeOrderService;
            OrderHandleResult orderHandleResult = queuedPlaceOrderService
                    .getPlaceOrderResult(userId, itemId, placeOrderTaskId);

            if (!orderHandleResult.isSuccess()) {
                log.info("App getOrder failed: {},{},{}", userId, itemId, placeOrderTaskId);
                return AppSingleResult.error(orderHandleResult.getCode(),
                        orderHandleResult.getMessage(), orderHandleResult);
            }

            log.info("App getOrder ok: {},{},{}", userId, itemId, placeOrderTaskId);
            return AppSingleResult.ok(orderHandleResult);
        } else {
            log.info("App getOrder failed: {},{},{}", userId, itemId, placeOrderTaskId);
            return AppSingleResult.error(AppErrorCode.ORDER_TYPE_NOT_SUPPORT);
        }
    }

    @Override
    public AppMultiResult<SaleOrderDTO> listOrdersByUser(Long userId, SaleOrdersQuery saleOrdersQuery) {
        log.info("App listOrdersByUser: {},{}", userId, saleOrdersQuery);

        // Get orders and convert to DTOs
        PageQuery condition = SaleOrderAppConverter.toQuery(saleOrdersQuery);
        PageResult<SaleOrder> orderPageResult = saleOrderDomainService.getOrdersByUserId(userId, condition);
        List<SaleOrderDTO> saleOrderDTOS = orderPageResult
                .getData()
                .stream()
                .map(SaleOrderAppConverter::toDTO)
                .collect(Collectors.toList());

        log.info("App listOrdersByUser ok: {},{}", userId, saleOrdersQuery);
        return AppMultiResult.of(saleOrderDTOS, orderPageResult.getTotal());
    }

    @Override
    @Transactional
    public AppResult cancelOrder(Long userId, Long orderId) {
        log.info("App cancelOrder: {},{}", userId, orderId);
        if (userId == null || orderId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Get order and validate
        SaleOrder saleOrder = saleOrderDomainService.getOrder(userId, orderId);
        if (saleOrder == null) {
            throw new BizException(AppErrorCode.ORDER_NOT_FOUND);
        }

        // Cancel order
        boolean cancelSuccess = saleOrderDomainService.cancelOrder(userId, orderId);
        if (!cancelSuccess) {
            log.info("App cancelOrder failed: {},{}", userId, orderId);
            return AppResult.error(AppErrorCode.ORDER_CANCEL_FAILED);
        }

        // Build stock deduction object
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(saleOrder.getItemId())
                .setQuantity(saleOrder.getQuantity())
                .setUserId(userId);

        // Revert stock
        boolean revertSuccess = stockDomainService.revertStock(stockDeduction);
        if (!revertSuccess) {
            log.info("App cancelOrder revert failed: {},{}", userId, orderId);
            throw new BizException(AppErrorCode.ORDER_CANCEL_FAILED);
        }

        // Revert stock in cache
        boolean cacheRevertSuccess = stockCacheService.revertStock(stockDeduction);
        if (!cacheRevertSuccess) {
            log.info("App cancelOrder revert failed in cache: {},{}", userId, orderId);
            throw new BizException(AppErrorCode.ORDER_CANCEL_FAILED);
        }

        log.info("App cancelOrder ok: {},{}", userId, orderId);
        return AppResult.ok();
    }

    private String buildPlaceOrderLockKey(Long userId) {
        return KeyUtil.link(PLACE_ORDER_LOCK_KEY, userId);
    }
}
