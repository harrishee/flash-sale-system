package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.model.enums.FlashOrderStatus;
import com.harris.domain.event.flashOrder.FlashOrderEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.event.flashOrder.FlashOrderEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.repository.FlashOrderRepository;
import com.harris.domain.service.FssOrderDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FssOrderDomainServiceImpl implements FssOrderDomainService {
    @Resource
    private FlashOrderRepository flashOrderRepository;
    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SaleOrder getOrder(Long userId, Long orderId) {
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleOrder> flashOrderOptional = flashOrderRepository.findOrderById(orderId);
        if (!flashOrderOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        return flashOrderOptional.get();
    }

    @Override
    public PageResult<SaleOrder> getOrdersByUserId(Long userId, PageQueryCondition pageQueryCondition) {
        if (pageQueryCondition == null) {
            pageQueryCondition = new PageQueryCondition();
        }
        List<SaleOrder> saleOrders = flashOrderRepository.findOrdersByCondition(pageQueryCondition.validateParams());
        int total = flashOrderRepository.countOrdersByCondition(pageQueryCondition.validateParams());
        return PageResult.with(saleOrders, total);
    }

    @Override
    public boolean placeOrder(Long userId, SaleOrder saleOrder) {
        log.info("placeOrder TRY: {},{}", userId, JSON.toJSONString(saleOrder));
        if (saleOrder == null || saleOrder.invalidParams()) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        saleOrder.setStatus(FlashOrderStatus.CREATED.getCode());
        boolean saveSuccess = flashOrderRepository.saveOrder(saleOrder);
        if (saveSuccess) {
            FlashOrderEvent flashOrderEvent = new FlashOrderEvent();
            flashOrderEvent.setFlashOrderEventType(FlashOrderEventType.CREATED);
            domainEventPublisher.publish(flashOrderEvent);
        }
        log.info("placeOrder DONE: {},{}", userId, JSON.toJSONString(saleOrder));
        return saveSuccess;
    }

    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        log.info("cancelOrder TRY: {},{}", userId, orderId);
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleOrder> flashOrderOptional = flashOrderRepository.findOrderById(orderId);
        if (!flashOrderOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        SaleOrder saleOrder = flashOrderOptional.get();
        if (!saleOrder.getUserId().equals(userId)) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        if (FlashOrderStatus.isCanceled(saleOrder.getStatus())) {
            return false;
        }
        saleOrder.setStatus(FlashOrderStatus.CANCELED.getCode());
        boolean saveSuccess = flashOrderRepository.updateStatusForOrder(saleOrder);
        if (saveSuccess) {
            FlashOrderEvent flashOrderEvent = new FlashOrderEvent();
            flashOrderEvent.setFlashOrderEventType(FlashOrderEventType.CANCEL);
            domainEventPublisher.publish(flashOrderEvent);
        }
        log.info("cancelOrder DONE: {},{}", userId, orderId);
        return saveSuccess;
    }
}
