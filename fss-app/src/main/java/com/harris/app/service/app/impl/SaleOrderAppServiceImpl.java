package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.SaleOrderAppService;
import com.harris.app.service.app.SecurityService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.app.util.AppConverter;
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
    // 分布式锁的 key 的前缀
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
        log.info("应用层 placeOrder: [{},{}]", userId, placeOrderCommand);
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildPlaceOrderLockKey(userId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return AppSingleResult.error(AppErrorCode.LOCK_FAILED.getErrCode(), AppErrorCode.LOCK_FAILED.getErrDesc());
            }
            
            // 检查用户是否通过风控检查，这里只是为了演示，直接返回 true
            boolean notRisk = securityService.inspectRisksByPolicy(userId);
            if (!notRisk) {
                log.info("应用层 placeOrder，风控检查失败: [{},{}]", userId, placeOrderCommand);
                return AppSingleResult.error(AppErrorCode.PLACE_ORDER_FAILED);
            }
            
            // 调用下单服务
            PlaceOrderResult placeOrderResult = placeOrderService.doPlaceOrder(userId, placeOrderCommand);
            if (!placeOrderResult.isSuccess()) {
                log.info("应用层 placeOrder，失败: [{},{}]", userId, placeOrderCommand);
                return AppSingleResult.error(placeOrderResult.getCode(), placeOrderResult.getMessage());
            }
            
            log.info("应用层 placeOrder，成功: [{},{}]", userId, placeOrderCommand);
            return AppSingleResult.ok(placeOrderResult);
        } catch (Exception e) {
            log.error("应用层 placeOrder，异常: ", e);
            return AppSingleResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public AppSingleResult<OrderHandleResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId) {
        log.info("应用层 getPlaceOrderTaskResult: [{},{},{}]", userId, itemId, placeOrderTaskId);
        if (userId == null || itemId == null || StringUtils.isEmpty(placeOrderTaskId)) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 如果下单服务是 QueuedPlaceOrderService 类型的
        if (placeOrderService instanceof QueuedPlaceOrderService) {
            // 强制转换为 QueuedPlaceOrderService 类型，然后调用其 getPlaceOrderResult 方法
            QueuedPlaceOrderService queuedPlaceOrderService = (QueuedPlaceOrderService) placeOrderService;
            OrderHandleResult orderHandleResult = queuedPlaceOrderService.getPlaceOrderResult(userId, itemId, placeOrderTaskId);
            
            if (!orderHandleResult.isSuccess()) {
                log.info("应用层 getPlaceOrderTaskResult，失败: [{},{},{}]", userId, itemId, placeOrderTaskId);
                return AppSingleResult.error(orderHandleResult.getCode(), orderHandleResult.getMessage(), orderHandleResult);
            }
            
            log.info("应用层 getPlaceOrderTaskResult，成功: [{},{},{}]", userId, itemId, placeOrderTaskId);
            return AppSingleResult.ok(orderHandleResult);
        } else {
            log.info("应用层 getPlaceOrderTaskResult，下单类型不支持: [{},{},{}]", userId, itemId, placeOrderTaskId);
            return AppSingleResult.error(AppErrorCode.ORDER_TYPE_NOT_SUPPORT);
        }
    }
    
    @Override
    public AppMultiResult<SaleOrderDTO> listOrdersByUser(Long userId, SaleOrdersQuery saleOrdersQuery) {
        log.info("应用层 listOrdersByUser: [{},{}]", userId, saleOrdersQuery);
        
        // 调用领域服务获取订单列表
        PageResult<SaleOrder> orderPageResult = saleOrderDomainService.getOrders(userId, AppConverter.toPageQuery(saleOrdersQuery));
        
        // 转换领域对象为 DTO
        List<SaleOrderDTO> saleOrderDTOS = orderPageResult.getData().stream().map(AppConverter::toDTO).collect(Collectors.toList());
        
        log.info("应用层 listOrdersByUser，成功: [{},{}]", userId, saleOrdersQuery);
        return AppMultiResult.of(saleOrderDTOS, orderPageResult.getTotal());
    }
    
    @Override
    @Transactional
    public AppResult cancelOrder(Long userId, Long orderId) {
        log.info("应用层 cancelOrder: [{},{}]", userId, orderId);
        if (userId == null || orderId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 调用领域服务获取订单
        SaleOrder saleOrder = saleOrderDomainService.getOrder(userId, orderId);
        if (saleOrder == null) {
            log.info("应用层 cancelOrder 订单不存在: [{},{}]", userId, orderId);
            throw new BizException(AppErrorCode.ORDER_NOT_FOUND);
        }
        
        // 调用领域服务取消订单
        boolean cancelSuccess = saleOrderDomainService.cancelOrder(userId, orderId);
        if (!cancelSuccess) {
            log.info("应用层 cancelOrder 订单取消失败: [{},{}]", userId, orderId);
            return AppResult.error(AppErrorCode.ORDER_CANCEL_FAILED);
        }
        
        // 创建库存扣减对象
        StockDeduction stockDeduction = new StockDeduction().setItemId(saleOrder.getItemId()).setQuantity(saleOrder.getQuantity()).setUserId(userId);
        
        // 调用领域服务恢复库存
        boolean revertSuccess = stockDomainService.revertStock(stockDeduction);
        if (!revertSuccess) {
            log.info("应用层 cancelOrder 库存恢复失败: [{},{}]", userId, orderId);
            throw new BizException(AppErrorCode.ORDER_CANCEL_FAILED);
        }
        
        // 调用缓存服务恢复库存
        boolean cacheRevertSuccess = stockCacheService.revertStock(stockDeduction);
        if (!cacheRevertSuccess) {
            log.info("应用层 cancelOrder Redis库存恢复失败: [{},{}]", userId, orderId);
            throw new BizException(AppErrorCode.ORDER_CANCEL_FAILED);
        }
        
        log.info("应用层 cancelOrder 订单取消成功: [{},{}]", userId, orderId);
        return AppResult.ok();
    }
    
    // 构建用于分布式锁的 key
    private String buildPlaceOrderLockKey(Long userId) {
        return KeyUtil.link(PLACE_ORDER_LOCK_KEY, userId);
    }
}
