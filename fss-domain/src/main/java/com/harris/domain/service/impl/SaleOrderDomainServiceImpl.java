package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.model.enums.SaleOrderStatus;
import com.harris.domain.model.enums.SaleOrderEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.model.event.SaleOrderEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;
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
        // Validate params
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find order from repository and validate
        Optional<SaleOrder> optionalSaleOrder = saleOrderRepository.findOrderById(orderId);
        if (!optionalSaleOrder.isPresent()) {
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }
        return optionalSaleOrder.get();
    }

    @Override
    public PageResult<SaleOrder> getOrdersByUserId(Long userId, PageQueryCondition pageQueryCondition) {
        // Validate params
        if (pageQueryCondition == null) {
            pageQueryCondition = new PageQueryCondition();
        }

        // Find orders with condition
        List<SaleOrder> saleOrders = saleOrderRepository.findOrdersByCondition(pageQueryCondition.validateParams());
        int total = saleOrderRepository.countOrdersByCondition(pageQueryCondition.validateParams());
        return PageResult.with(saleOrders, total);
    }

    @Override
    public boolean placeOrder(Long userId, SaleOrder saleOrder) {
        log.info("placeOrder: {},{}", userId, JSON.toJSONString(saleOrder));

        // Validate params
        if (saleOrder == null || saleOrder.invalidParams()) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Set status to created and save order
        saleOrder.setStatus(SaleOrderStatus.CREATED.getCode());
        boolean saveSuccess = saleOrderRepository.saveOrder(saleOrder);

        // Publish event if save success
        if (saveSuccess) {
            SaleOrderEvent saleOrderEvent = new SaleOrderEvent();
            saleOrderEvent.setSaleOrderEventType(SaleOrderEventType.CREATED);
            domainEventPublisher.publish(saleOrderEvent);
            log.info("placeOrder, place order event published: {},{}", userId, JSON.toJSONString(saleOrder));
        }
        log.info("placeOrder, order created: {},{}", userId, JSON.toJSONString(saleOrder));
        return saveSuccess;
    }

    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        log.info("cancelOrder: {},{}", userId, orderId);

        // Validate params
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find order from repository and validate
        Optional<SaleOrder> optionalSaleOrder = saleOrderRepository.findOrderById(orderId);
        if (!optionalSaleOrder.isPresent()) {
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }

        // Check if order belongs to user
        SaleOrder saleOrder = optionalSaleOrder.get();
        if (!saleOrder.getUserId().equals(userId)) {
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }

        // Return if order is already canceled
        if (SaleOrderStatus.isCanceled(saleOrder.getStatus())) {
            return false;
        }

        // Set status to canceled and save order
        saleOrder.setStatus(SaleOrderStatus.CANCELED.getCode());
        boolean updateSuccess = saleOrderRepository.updateStatus(saleOrder);
        if (updateSuccess) {
            SaleOrderEvent saleOrderEvent = new SaleOrderEvent();
            saleOrderEvent.setSaleOrderEventType(SaleOrderEventType.CANCEL);
            domainEventPublisher.publish(saleOrderEvent);
            log.info("cancelOrder, cancel order event published: {},{}", userId, JSON.toJSONString(saleOrder));
        }
        log.info("cancelOrder, order canceled: {},{}", userId, orderId);
        return updateSuccess;
    }
}
