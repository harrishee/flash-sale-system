package com.harris.app.service.app.impl;

import com.harris.app.exception.BizException;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.converter.FlashOrderAppConverter;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.security.SecurityService;
import com.harris.app.service.app.FlashOrderAppService;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.cache.ItemStockCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.FlashOrder;
import com.harris.domain.service.FlashOrderDomainService;
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

import static com.harris.app.exception.AppErrCode.*;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashOrderAppServiceImpl implements FlashOrderAppService {
    private static final String PLACE_ORDER_LOCK_KEY = "PLACE_ORDER_LOCK_KEY";

    @Resource
    private SecurityService securityService;

    @Resource
    private FlashOrderDomainService flashOrderDomainService;

    @Resource
    private StockDomainService stockDomainService;

    @Resource
    private PlaceOrderService placeOrderService;

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    @Transactional
    public AppSingleResult<PlaceOrderResult> placeOrder(Long userId, FlashPlaceOrderCommand flashPlaceOrderCommand) {
        if (userId == null || flashPlaceOrderCommand == null || flashPlaceOrderCommand.invalidParams()) {
            throw new BizException(INVALID_PARAMS);
        }
        String placeOrderLockKey = buildPlaceOrderLockKey(userId);
        DistributedLock placeOrderLock = distributedLockService.getDistributedLock(placeOrderLockKey);
        try {
            boolean isLocked = placeOrderLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return AppSingleResult.error(FREQUENTLY_ERROR.getErrCode(), FREQUENTLY_ERROR.getErrDesc());
            }
            boolean isPassRiskInspect = securityService.inspectRisksByPolicy(userId);
            if (!isPassRiskInspect) {
                return AppSingleResult.error(PLACE_ORDER_FAILED);
            }
            PlaceOrderResult placeOrderResult = placeOrderService.doPlaceOrder(userId, flashPlaceOrderCommand);
            if (!placeOrderResult.isSuccess()) {
                return AppSingleResult.error(placeOrderResult.getCode(), placeOrderResult.getMsg());
            }
            return AppSingleResult.ok(placeOrderResult);
        } catch (Exception e) {
            return AppSingleResult.error(PLACE_ORDER_FAILED);
        } finally {
            placeOrderLock.unlock();
        }
    }

    @Override
    public AppSingleResult<OrderTaskHandleResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId) {
        if (userId == null || itemId == null || StringUtils.isEmpty(placeOrderTaskId)) {
            throw new BizException(INVALID_PARAMS);
        }
        if (placeOrderService instanceof QueuedPlaceOrderService) {
            QueuedPlaceOrderService queuedPlaceOrderService = (QueuedPlaceOrderService) placeOrderService;
            OrderTaskHandleResult orderTaskHandleResult = queuedPlaceOrderService.getPlaceOrderResult(userId, itemId, placeOrderTaskId);
            if (!orderTaskHandleResult.isSuccess()) {
                return AppSingleResult.error(orderTaskHandleResult.getCode(), orderTaskHandleResult.getMsg(), orderTaskHandleResult);
            }
            return AppSingleResult.ok(orderTaskHandleResult);
        } else {
            return AppSingleResult.error(ORDER_TYPE_NOT_SUPPORT);
        }
    }

    @Override
    public AppMultiResult<FlashOrderDTO> getOrdersByUser(Long userId, FlashOrdersQuery flashOrdersQuery) {
        PageResult<FlashOrder> flashOrderPageResult = flashOrderDomainService.getOrdersByUserId(userId, FlashOrderAppConverter.toQuery(flashOrdersQuery));
        List<FlashOrderDTO> flashOrderDTOS = flashOrderPageResult.getData().stream().map(FlashOrderAppConverter::toDTO).collect(Collectors.toList());
        return AppMultiResult.of(flashOrderPageResult.getTotal(), flashOrderDTOS);
    }

    @Override
    @Transactional
    public AppResult cancelOrder(Long userId, Long orderId) {
        if (userId == null || orderId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        FlashOrder flashOrder = flashOrderDomainService.getOrder(userId, orderId);
        if (flashOrder == null) {
            throw new BizException(ORDER_NOT_FOUND);
        }
        boolean cancelSuccess = flashOrderDomainService.cancelOrder(userId, orderId);
        if (!cancelSuccess) {
            return AppResult.error(ORDER_CANCEL_FAILED);
        }
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(flashOrder.getItemId())
                .setQuantity(flashOrder.getQuantity())
                .setUserId(userId);

        boolean stockRecoverSuccess = stockDomainService.increaseItemStock(stockDeduction);
        if (!stockRecoverSuccess) {
            throw new BizException(ORDER_CANCEL_FAILED);
        }
        boolean stockInRedisRecoverSuccess = itemStockCacheService.increaseItemStock(stockDeduction);
        if (!stockInRedisRecoverSuccess) {
            throw new BizException(ORDER_CANCEL_FAILED);
        }
        return AppResult.ok();
    }

    private String buildPlaceOrderLockKey(Long userId) {
        return link(PLACE_ORDER_LOCK_KEY, userId);
    }
}
