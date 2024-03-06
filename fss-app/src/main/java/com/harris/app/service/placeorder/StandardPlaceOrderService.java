package com.harris.app.service.placeorder;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.model.result.PlaceOrderResult;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.app.service.app.SaleItemAppService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.app.util.AppConverter;
import com.harris.app.util.OrderUtil;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.service.SaleOrderDomainService;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "standard")
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
        log.info("同步下单模式已启用");
    }
    
    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        if (userId == null || placeOrderCommand == null || placeOrderCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 检查活动是否允许下单
        boolean activityAllowed = saleActivityAppService.isPlaceOrderAllowed(placeOrderCommand.getActivityId());
        if (!activityAllowed) {
            log.info("同步下单, 活动不允许下单: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        }
        // 检查商品是否允许下单
        boolean itemAllowed = saleItemAppService.isPlaceOrderAllowed(placeOrderCommand.getItemId());
        if (!itemAllowed) {
            log.info("同步下单, 商品不允许下单: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED);
        }
        
        // 获取商品
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(placeOrderCommand.getItemId());
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            log.info("同步下单, 获取商品失败: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
            return PlaceOrderResult.error(AppErrorCode.GET_ITEM_FAILED);
        }
        
        // 用 snowflake 算法生成订单号
        Long orderId = OrderUtil.generateOrderNo();
        
        // 构建订单对象
        SaleItemDTO saleItemDTO = itemResult.getData();
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
        
        // 缓存预扣标识
        boolean preDeductSuccess = false;
        try {
            // 1. 缓存预扣
            preDeductSuccess = stockCacheService.deductStock(stockDeduction);
            if (!preDeductSuccess) {
                log.info("同步下单, 缓存预扣失败: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
                return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED.getErrCode(), AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
            
            // 2. 扣减库存
            boolean deductSuccess = stockDomainService.deductStock(stockDeduction);
            if (!deductSuccess) {
                log.info("同步下单, 扣减库存失败: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
                return PlaceOrderResult.error(AppErrorCode.PLACE_ORDER_FAILED.getErrCode(), AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
            }
            
            // 3. 调用领域服务的 创建订单 方法
            boolean placeOrderSuccess = saleOrderDomainService.createOrder(userId, newOrder);
            if (!placeOrderSuccess) throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
        } catch (Exception e) {
            // 如果缓存预扣成功，但是扣减库存或者创建订单失败，需要恢复缓存库存
            if (preDeductSuccess) {
                boolean revertSuccess = stockCacheService.revertStock(stockDeduction);
                if (!revertSuccess) {
                    log.error("同步下单, 恢复缓存预扣失败: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand);
                }
            }
            
            log.error("同步下单, 下单失败: [userId={}, placeOrderCommand={}]", userId, placeOrderCommand, e);
            throw new BizException(AppErrorCode.PLACE_ORDER_FAILED.getErrDesc());
        }
        
        log.info("同步下单, 下单成功: [userId={}, orderId={}]", userId, orderId);
        return PlaceOrderResult.ok(orderId);
    }
}
