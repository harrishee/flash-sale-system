package com.harris.domain.service.order;

import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.model.enums.SaleOrderEventType;
import com.harris.domain.model.enums.SaleOrderStatus;
import com.harris.domain.model.event.SaleOrderEvent;
import com.harris.domain.repository.SaleOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SaleOrderDomainServiceImpl implements SaleOrderDomainService {
    @Resource
    private SaleOrderRepository saleOrderRepository;
    @Resource
    private DomainEventPublisher domainEventPublisher;
    
    @Override
    public SaleOrder getOrder(Long userId, Long orderId) {
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        
        // 从仓库中获取订单
        Optional<SaleOrder> saleOrder = saleOrderRepository.findOrderById(orderId);
        if (!saleOrder.isPresent()) throw new DomainException(DomainErrorCode.ITEM_NOT_EXIST);
        return saleOrder.get();
    }
    
    @Override
    public PageResult<SaleOrder> getOrders(Long userId, PageQuery pageQuery) {
        if (pageQuery == null) pageQuery = new PageQuery();
        
        // 从仓库中获取 订单列表 和 订单数量，包装成分页结果并返回
        List<SaleOrder> saleOrders = saleOrderRepository.findAllOrderByCondition(pageQuery.validateParams());
        int total = saleOrderRepository.countAllOrderByCondition(pageQuery.validateParams());
        return PageResult.of(saleOrders, total);
    }
    
    @Override
    public boolean createOrder(Long userId, SaleOrder saleOrder) {
        if (saleOrder == null || saleOrder.invalidParams()) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        
        // 设置订单状态为已创建并保存订单
        saleOrder.setStatus(SaleOrderStatus.CREATED.getCode());
        boolean saveSuccess = saleOrderRepository.saveOrder(saleOrder);
        if (saveSuccess) {
            // 创建订单创建事件
            SaleOrderEvent saleOrderEvent = new SaleOrderEvent();
            saleOrderEvent.setOrderId(saleOrder.getId());
            saleOrderEvent.setSaleOrderEventType(SaleOrderEventType.CREATED);
            
            // 发布订单创建事件
            domainEventPublisher.publish(saleOrderEvent);
            // log.info("领域层，createOrder, 订单创建事件发布成功: [saleOrderEvent={}]", saleOrderEvent);
        }
        
        return saveSuccess;
    }
    
    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        
        // 从仓库中获取订单
        Optional<SaleOrder> optionalSaleOrder = saleOrderRepository.findOrderById(orderId);
        if (!optionalSaleOrder.isPresent()) {
            log.info("领域层 cancelOrder, 订单不存在: [userId={}, orderId={}]", userId, orderId);
            throw new DomainException(DomainErrorCode.ORDER_NOT_EXIST);
        }
        
        // 检查订单是否属于当前用户
        SaleOrder saleOrder = optionalSaleOrder.get();
        if (!saleOrder.getUserId().equals(userId)) {
            log.info("领域层 cancelOrder, 订单不属于当前用户: [userId={}, orderId={}]", userId, orderId);
            throw new DomainException(DomainErrorCode.ORDER_NOT_EXIST);
        }
        // 检查订单是否已取消
        if (SaleOrderStatus.isCanceled(saleOrder.getStatus())) {
            return false;
        }
        
        // 设置订单状态为已取消并更新订单
        saleOrder.setStatus(SaleOrderStatus.CANCELED.getCode());
        boolean updateSuccess = saleOrderRepository.updateStatus(saleOrder);
        if (updateSuccess) {
            // 创建订单取消事件
            SaleOrderEvent saleOrderEvent = new SaleOrderEvent();
            saleOrderEvent.setSaleOrderEventType(SaleOrderEventType.CANCEL);
            
            // 发布订单取消事件
            domainEventPublisher.publish(saleOrderEvent);
            log.info("领域层 cancelOrder, 订单取消事件发布成功: [saleOrderEvent={}]", saleOrderEvent);
        }
        
        return updateSuccess;
    }
}
