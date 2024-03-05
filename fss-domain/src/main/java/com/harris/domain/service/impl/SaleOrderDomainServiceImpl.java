package com.harris.domain.service.impl;

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
import com.harris.domain.service.SaleOrderDomainService;
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
        if (userId == null || orderId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        
        // 从仓库中获取订单
        Optional<SaleOrder> optionalSaleOrder = saleOrderRepository.findOrderById(orderId);
        if (!optionalSaleOrder.isPresent()) throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        return optionalSaleOrder.get();
    }
    
    @Override
    public PageResult<SaleOrder> getOrders(Long userId, PageQuery pageQuery) {
        if (pageQuery == null) pageQuery = new PageQuery();
        
        // 从仓库中获取订单列表和订单数量，包装成分页结果并返回
        List<SaleOrder> saleOrders = saleOrderRepository.findAllOrderByCondition(pageQuery.validateParams());
        int total = saleOrderRepository.countOrdersByCondition(pageQuery.validateParams());
        return PageResult.of(saleOrders, total);
    }
    
    @Override
    public boolean createOrder(Long userId, SaleOrder saleOrder) {
        if (saleOrder == null || saleOrder.invalidParams()) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        // log.info("领域层服务 createOrder: [userId={}, itemId={}, activityId={}]", userId, saleOrder.getItemId(), saleOrder.getActivityId());
        
        // 设置订单状态为已创建并保存订单
        saleOrder.setStatus(SaleOrderStatus.CREATED.getCode());
        boolean saveSuccess = saleOrderRepository.saveOrder(saleOrder);
        // log.info("领域层服务 createOrder, 1. 保存订单到仓库: [userId={}, itemId={}, activityId={}]", userId, saleOrder.getItemId(), saleOrder.getActivityId());
        
        // 如果保存成功，发布订单创建事件
        if (saveSuccess) {
            SaleOrderEvent saleOrderEvent = new SaleOrderEvent();
            saleOrderEvent.setSaleOrderEventType(SaleOrderEventType.CREATED);
            domainEventPublisher.publish(saleOrderEvent);
            // log.info("领域层服务，createOrder, 2. 订单创建事件发布成功: [saleOrderEvent={}]", saleOrderEvent);
        }
        
        return saveSuccess;
    }
    
    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        log.info("领域层服务 cancelOrder: [userId={}, orderId={}]", userId, orderId);
        if (userId == null || orderId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        
        // 从仓库中获取订单
        Optional<SaleOrder> optionalSaleOrder = saleOrderRepository.findOrderById(orderId);
        if (!optionalSaleOrder.isPresent()) {
            log.info("领域层服务 cancelOrder, 订单不存在: [userId={}, orderId={}]", userId, orderId);
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }
        
        // 检查订单是否属于当前用户
        SaleOrder saleOrder = optionalSaleOrder.get();
        if (!saleOrder.getUserId().equals(userId)) {
            log.info("领域层服务 cancelOrder, 订单不属于当前用户: [userId={}, orderId={}]", userId, orderId);
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }
        
        // 检查订单是否已取消
        if (SaleOrderStatus.isCanceled(saleOrder.getStatus())) {
            return false;
        }
        
        // 设置订单状态为已取消并更新订单
        saleOrder.setStatus(SaleOrderStatus.CANCELED.getCode());
        boolean updateSuccess = saleOrderRepository.updateStatus(saleOrder);
        // log.info("领域层服务 cancelOrder, 1. 更新订单取消到仓库: [userId={}, orderId={}]", userId, orderId);
        
        // 如果更新成功，发布订单取消事件
        if (updateSuccess) {
            SaleOrderEvent saleOrderEvent = new SaleOrderEvent();
            saleOrderEvent.setSaleOrderEventType(SaleOrderEventType.CANCEL);
            domainEventPublisher.publish(saleOrderEvent);
            // log.info("领域层服务 cancelOrder, 2. 订单取消事件发布成功: [saleOrderEvent={}]", saleOrderEvent);
        }
        
        return updateSuccess;
    }
}
