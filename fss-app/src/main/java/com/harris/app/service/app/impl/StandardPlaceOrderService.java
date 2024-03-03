package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.PlaceOrderService;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.app.service.app.SaleItemAppService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.app.util.AppConverter;
import com.harris.app.util.OrderUtil;
import com.harris.app.util.PlaceOrderCondition;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.SaleOrderDomainService;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Service
@Conditional(PlaceOrderCondition.class)
public class StandardPlaceOrderService implements PlaceOrderService {
    @Resource
    private SaleOrderDomainService saleOrderDomainService;
    
    @Resource
    private StockDomainService stockDomainService;
    
    @Resource
    private SaleActivityAppService saleActivityAppService;
    
    @Resource
    private SaleItemAppService saleItemAppService;
    
    @Resource
    private StockCacheService stockCacheService;
    
    @PostConstruct
    public void init() {
        log.info("应用层 standard PlaceOrderService initialized");
    }
    
    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        log.info("应用层 standard doPlaceOrder: [{},{}]", userId, placeOrderCommand);
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 检查活动是否允许下单
        boolean activityAllowed = saleActivityAppService.isPlaceOrderAllowed(placeOrderCommand.getActivityId());
        if (!activityAllowed) {
            log.info("应用层 standard doPlaceOrder, 活动不允许下单: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        }
        
        // 检查商品是否允许下单
        boolean itemAllowed = saleItemAppService.isPlaceOrderAllowed(placeOrderCommand.getItemId());
        if (!itemAllowed) {
            log.info("应用层 standard doPlaceOrder, 商品不允许下单: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        }
        
        // 获取商品信息
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(placeOrderCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("应用层 standard doPlaceOrder, 获取商品信息失败: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.GET_ITEM_FAILED);
        }
        
        // 检查商品是否在售
        SaleItemDTO saleItemDTO = itemResult.getData();
        if (saleItemDTO.notOnSale()) {
            log.info("应用层 standard doPlaceOrder, 商品不在售: [{},{}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.ITEM_NOT_ON_SALE);
        }
        
        // 生成订单号（snowflake 算法）
        Long orderId = OrderUtil.generateOrderNo();
        
        // 构建订单对象
        SaleOrder newOrder = AppConverter.toDomainModel(placeOrderCommand);
        newOrder.setItemTitle(saleItemDTO.getItemTitle());
        newOrder.setSalePrice(saleItemDTO.getSalePrice());
        newOrder.setUserId(userId);
        newOrder.setId(orderId);
        
        // 构建库存扣减对象
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(placeOrderCommand.getItemId())
                .setQuantity(placeOrderCommand.getQuantity())
                .setUserId(userId);
        
        // 预扣减库存成功标识，默认为失败
        boolean preDeductSuccess = false;
        try {
            // 1. 预扣减库存
            preDeductSuccess = stockCacheService.deductStock(stockDeduction);
            if (!preDeductSuccess) {
                log.info("应用层 standard doPlaceOrder, 预扣减库存失败: [{},{}]", userId, placeOrderCommand);
                return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED.getErrCode(), AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
            
            // 2. 扣减库存
            boolean deductSuccess = stockDomainService.deductStock(stockDeduction);
            if (!deductSuccess) {
                log.info("应用层 standard doPlaceOrder, 扣减库存失败: [{},{}]", userId, placeOrderCommand);
                return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED.getErrCode(), AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
            
            // 3. 下单
            boolean placeOrderSuccess = saleOrderDomainService.placeOrder(userId, newOrder);
            if (!placeOrderSuccess) throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
        } catch (Exception e) {
            // 如果预扣减成功，但是扣减库存或下单失败，需要恢复缓存库存
            if (preDeductSuccess) {
                boolean revertSuccess = stockCacheService.revertStock(stockDeduction);
                if (!revertSuccess)
                    log.error("应用层 standard doPlaceOrder, 恢复缓存库存失败: [{},{}]", userId, placeOrderCommand);
            }
            
            log.error("应用层 standard doPlaceOrder, 下单失败: [{},{}]", userId, placeOrderCommand);
            throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
        }
        
        log.info("应用层 standard doPlaceOrder, 下单成功: [{},{}]", userId, placeOrderCommand);
        return PlaceOrderResult.ok(orderId);
    }
}
