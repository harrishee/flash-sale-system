package com.harris.app.service.placeorder;

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
import com.harris.app.service.saleactivity.SaleActivityAppService;
import com.harris.app.service.saleitem.SaleItemAppService;
import com.harris.app.util.AppConverter;
import com.harris.app.util.OrderUtil;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.item.SaleItemDomainService;
import com.harris.domain.service.order.SaleOrderDomainService;
import com.harris.domain.service.stock.StockDomainService;
import com.harris.infra.distributed.cache.RedisCacheService;
import com.harris.infra.util.KeyUtil;
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
    private static final String PLACE_ORDER_TASK_ORDER_ID_KEY = "PLACE_ORDER_TASK_ORDER_ID_KEY";
    
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
        log.info("队列下单服务已启用");
    }
    
    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            return PlaceOrderResult.error(AppErrorCode.INVALID_PARAMS);
        }
        // log.info("队列下单 doPlaceOrder 开始: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
        
        // 获取商品
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(placeOrderCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("队列下单 doPlaceOrder, 获取商品失败: [itemId={}]", placeOrderCommand.getItemId());
            return PlaceOrderResult.error(AppErrorCode.GET_ITEM_FAILED);
        }
        
        // 检查商品是否在售
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (saleItemDTO.notOnSale()) {
            log.info("队列下单 doPlaceOrder, 商品不在售: [itemId={}]", placeOrderCommand.getItemId());
            return PlaceOrderResult.error(AppErrorCode.ITEM_NOT_ON_SALE);
        }
        
        // 根据 用户ID + 商品ID 生成 下单任务ID
        String placeOrderTaskId = OrderUtil.getPlaceOrderTaskId(userId, placeOrderCommand.getItemId());
        PlaceOrderTask placeOrderTask = AppConverter.toTask(userId, placeOrderCommand);
        placeOrderTask.setPlaceOrderTaskId(placeOrderTaskId);
        
        // 调用队列下单任务服务的 提交下单任务 方法
        OrderSubmitResult submitResult = placeOrderTaskService.submit(placeOrderTask);
        if (!submitResult.isSuccess()) {
            return PlaceOrderResult.error(submitResult.getCode(), submitResult.getMessage());
        }
        
        return PlaceOrderResult.ok(placeOrderTaskId);
    }
    
    @Transactional
    public void handlePlaceOrderTask(PlaceOrderTask placeOrderTask) {
        try {
            Long userId = placeOrderTask.getUserId();
            
            // 检查活动是否允许下单
            boolean activityAllowed = saleActivityAppService.isPlaceOrderAllowed(placeOrderTask.getActivityId());
            if (!activityAllowed) {
                log.info("队列下单 handlePlaceOrderTask, 活动不允许下单: [userId={}, placeOrderTask={}]", userId, placeOrderTask);
                placeOrderTaskService.updatePlaceOrderTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            
            // 检查商品是否允许下单
            boolean itemAllowed = saleItemAppService.isPlaceOrderAllowed(placeOrderTask.getItemId());
            if (!itemAllowed) {
                log.info("队列下单 handlePlaceOrderTask, 商品不允许下单: [userId={}, placeOrderTask={}]", userId, placeOrderTask);
                placeOrderTaskService.updatePlaceOrderTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            
            // 构建新订单对象
            SaleItem saleItem = saleItemDomainService.getItem(placeOrderTask.getItemId());
            Long orderId = OrderUtil.generateOrderNo();
            SaleOrder newOrder = AppConverter.toDomainModel(placeOrderTask);
            newOrder.setItemTitle(saleItem.getItemTitle());
            newOrder.setSalePrice(saleItem.getSalePrice());
            newOrder.setUserId(userId);
            newOrder.setId(orderId);
            
            // 构建库存扣减对象
            StockDeduction stockDeduction = new StockDeduction()
                    .setItemId(placeOrderTask.getItemId())
                    .setQuantity(placeOrderTask.getQuantity());
            
            // 1. 扣减库存
            boolean deductSuccess = stockDomainService.deductStock(stockDeduction);
            if (!deductSuccess) {
                log.info("队列下单 handlePlaceOrderTask, 抢购失败，库存不足: [userId={}, placeOrderTask={}]", userId, placeOrderTask);
                return;
            }
            
            // 2. 调用领域服务的 下单 方法（存入数据库）
            boolean createOrderSuccess = saleOrderDomainService.createOrder(userId, newOrder);
            if (!createOrderSuccess) {
                log.info("队列下单 handlePlaceOrderTask, 抢购失败，创建订单失败: [userId={}, placeOrderTask={}]", userId, placeOrderTask);
                throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
            
            // 更新下单任务的处理结果
            placeOrderTaskService.updatePlaceOrderTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), true);
            
            // 将订单ID放入缓存
            redisCacheService.put(buildPlaceOrderTaskKey(placeOrderTask.getPlaceOrderTaskId()), orderId, CacheConstant.HOURS_24);
            log.info("队列下单 handlePlaceOrderTask，抢购成功: [userId={}, placeOrderTask={}]", userId, placeOrderTask);
        } catch (Exception e) {
            // 更新下单任务的处理结果
            placeOrderTaskService.updatePlaceOrderTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
            log.error("队列下单 handlePlaceOrderTask, 下单任务处理异常: [placeOrderTask={}] ", placeOrderTask, e);
            throw new BizException(e.getMessage());
        }
    }
    
    public OrderHandleResult getOrderHandleResult(Long userId, Long itemId, String placeOrderTaskId) {
        // 检查下单任务ID是否有效
        String expectedTaskId = OrderUtil.getPlaceOrderTaskId(userId, itemId);
        if (!expectedTaskId.equals(placeOrderTaskId)) {
            return OrderHandleResult.error(AppErrorCode.PLACE_ORDER_TASK_ID_INVALID);
        }
        
        // 检查下单任务的处理状态
        PlaceOrderTaskStatus placeOrderTaskStatus = placeOrderTaskService.getStatus(placeOrderTaskId);
        if (placeOrderTaskStatus == null) {
            return OrderHandleResult.error(AppErrorCode.PLACE_ORDER_TASK_ID_INVALID);
        }
        
        // 检查下单任务是否处理成功
        if (!PlaceOrderTaskStatus.SUCCESS.equals(placeOrderTaskStatus)) {
            return OrderHandleResult.error(placeOrderTaskStatus);
        }
        
        // 从缓存中获取订单ID
        Long orderId = redisCacheService.get(buildPlaceOrderTaskKey(placeOrderTaskId), Long.class);
        return OrderHandleResult.ok(orderId);
    }
    
    private String buildPlaceOrderTaskKey(String placeOrderTaskId) {
        return KeyUtil.link(PLACE_ORDER_TASK_ORDER_ID_KEY, placeOrderTaskId);
    }
}
