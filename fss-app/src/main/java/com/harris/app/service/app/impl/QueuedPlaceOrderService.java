package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.PlaceOrderTaskStatus;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.OrderHandleResult;
import com.harris.app.model.result.OrderSubmitResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.PlaceOrderTaskService;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.app.service.app.SaleItemAppService;
import com.harris.app.util.AppConverter;
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
    // 分布式锁的 key 的前缀
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
        log.info("应用层 queued PlaceOrderService initialized");
    }
    
    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        log.info("应用层 queued doPlaceOrder: [{},{}]", userId, placeOrderCommand);
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            return PlaceOrderResult.error(AppErrorCode.INVALID_PARAMS);
        }
        
        // 获取商品信息
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(placeOrderCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("应用层 queued doPlaceOrder, 获取商品信息失败: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.GET_ITEM_FAILED);
        }
        
        // 检查商品是否在售
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (saleItemDTO.notOnSale()) {
            log.info("应用层 queued doPlaceOrder, 商品不在售: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.ITEM_NOT_ON_SALE);
        }
        
        // 生成 下单任务ID（MD5算法），并设置到下单任务对象中
        String placeOrderTaskId = OrderUtil.generateOrderTaskId(userId, placeOrderCommand.getItemId());
        PlaceOrderTask placeOrderTask = AppConverter.toTask(userId, placeOrderCommand);
        placeOrderTask.setPlaceOrderTaskId(placeOrderTaskId);
        
        // 提交下单任务到队列
        OrderSubmitResult submitResult = placeOrderTaskService.submit(placeOrderTask);
        if (!submitResult.isSuccess()) {
            log.info("应用层 queued doPlaceOrder, 提交任务失败: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(submitResult.getCode(), submitResult.getMessage());
        }
        
        log.info("应用层 queued doPlaceOrder, 提交任务完成: [{},{}]", userId, placeOrderCommand);
        return PlaceOrderResult.ok(placeOrderTaskId);
    }
    
    public OrderHandleResult getPlaceOrderResult(Long userId, Long itemId, String placeOrderTaskId) {
        // 检查下单任务ID是否和用户ID、商品ID匹配（MD5算法）
        String expectedTaskId = OrderUtil.generateOrderTaskId(userId, itemId);
        if (!expectedTaskId.equals(placeOrderTaskId)) {
            return OrderHandleResult.error(AppErrorCode.PLACE_ORDER_TASK_ID_INVALID);
        }
        
        // 检查下单任务的处理状态
        PlaceOrderTaskStatus placeOrderTaskStatus = placeOrderTaskService.getStatus(placeOrderTaskId);
        if (placeOrderTaskStatus == null) {
            return OrderHandleResult.error(AppErrorCode.PLACE_ORDER_TASK_ID_INVALID);
        }
        
        // 检查下单任务是否处理成功，如果没有成功，返回 OrderHandleResult 的失败结果
        if (!PlaceOrderTaskStatus.SUCCESS.equals(placeOrderTaskStatus)) {
            return OrderHandleResult.error(placeOrderTaskStatus);
        }
        
        // 从缓存中获取下单任务ID对应的订单ID
        Long orderId = redisCacheService.getObject(PL_TASK_ORDER_ID_KEY + placeOrderTaskId, Long.class);
        return OrderHandleResult.ok(orderId);
    }
    
    @Transactional
    public void handlePlaceOrderTask(PlaceOrderTask placeOrderTask) {
        try {
            Long userId = placeOrderTask.getUserId();
            
            // 检查活动是否允许下单
            boolean activityAllowed = saleActivityAppService.isPlaceOrderAllowed(placeOrderTask.getActivityId());
            if (!activityAllowed) {
                log.info("应用层 queued handlePlaceOrderTask, 活动不允许下单: [{}]", placeOrderTask);
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            
            // 检查商品是否允许下单
            boolean itemAllowed = saleItemAppService.isPlaceOrderAllowed(placeOrderTask.getItemId());
            if (!itemAllowed) {
                log.info("应用层 queued handlePlaceOrderTask, 商品不允许下单: [{}]", placeOrderTask);
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            
            // 获取商品信息
            SaleItem saleItem = saleItemDomainService.getItem(placeOrderTask.getItemId());
            
            // 生成订单号（snowflake 算法）
            Long orderId = OrderUtil.generateOrderNo();
            
            // 构建订单对象
            SaleOrder saleOrderToPlace = AppConverter.toDomainModel(placeOrderTask);
            saleOrderToPlace.setItemTitle(saleItem.getItemTitle());
            saleOrderToPlace.setSalePrice(saleItem.getSalePrice());
            saleOrderToPlace.setUserId(userId);
            saleOrderToPlace.setId(orderId);
            
            // 构建库存扣减对象
            StockDeduction stockDeduction = new StockDeduction()
                    .setItemId(placeOrderTask.getItemId())
                    .setQuantity(placeOrderTask.getQuantity());
            
            // 扣减库存
            boolean deductSuccess = stockDomainService.deductStock(stockDeduction);
            if (!deductSuccess) {
                log.info("应用层 queued handlePlaceOrderTask, 扣减库存失败: [{}]", placeOrderTask);
                return;
            }
            
            // 下单
            boolean placeOrderSuccess = saleOrderDomainService.placeOrder(userId, saleOrderToPlace);
            if (!placeOrderSuccess) {
                log.info("应用层 queued handlePlaceOrderTask, 下单失败: [{}]", placeOrderTask);
                throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
            
            // 更新下单任务的处理结果
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), true);
            
            // 将订单ID放入缓存
            redisCacheService.put(PL_TASK_ORDER_ID_KEY + placeOrderTask.getPlaceOrderTaskId(), orderId, CacheConstant.HOURS_24);
            log.info("应用层 queued handlePlaceOrderTask, 下单任务处理完成: [{}]", placeOrderTask);
        } catch (Exception e) {
            // 更新下单任务的处理结果
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
            log.error("应用层 queued handlePlaceOrderTask, 下单任务处理错误: [{}]", placeOrderTask, e);
            throw new BizException(e.getMessage());
        }
    }
}
