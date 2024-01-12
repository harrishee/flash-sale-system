package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.model.enums.FlashOrderStatus;
import com.harris.domain.event.flashOrder.FlashOrderEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.event.flashOrder.FlashOrderEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;
import com.harris.domain.repository.FlashOrderRepository;
import com.harris.domain.service.FlashOrderDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FlashOrderDomainServiceImpl implements FlashOrderDomainService {
    @Resource
    private FlashOrderRepository flashOrderRepository;
    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public FlashOrder getOrder(Long userId, Long orderId) {
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashOrder> flashOrderOptional = flashOrderRepository.findOrderById(orderId);
        if (!flashOrderOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        return flashOrderOptional.get();
    }

    @Override
    public PageResult<FlashOrder> getOrdersByUserId(Long userId, PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashOrder> flashOrders = flashOrderRepository.findOrdersByCondition(pagesQueryCondition.validateParams());
        int total = flashOrderRepository.countOrdersByCondition(pagesQueryCondition.validateParams());
        return PageResult.with(flashOrders, total);
    }

    @Override
    public boolean placeOrder(Long userId, FlashOrder flashOrder) {
        log.info("placeOrder TRY: {},{}", userId, JSON.toJSONString(flashOrder));
        if (flashOrder == null || flashOrder.invalidParams()) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        flashOrder.setStatus(FlashOrderStatus.CREATED.getCode());
        boolean saveSuccess = flashOrderRepository.saveOrder(flashOrder);
        if (saveSuccess) {
            FlashOrderEvent flashOrderEvent = new FlashOrderEvent();
            flashOrderEvent.setFlashOrderEventType(FlashOrderEventType.CREATED);
            domainEventPublisher.publish(flashOrderEvent);
        }
        log.info("placeOrder DONE: {},{}", userId, JSON.toJSONString(flashOrder));
        return saveSuccess;
    }

    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        log.info("cancelOrder TRY: {},{}", userId, orderId);
        if (userId == null || orderId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashOrder> flashOrderOptional = flashOrderRepository.findOrderById(orderId);
        if (!flashOrderOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        FlashOrder flashOrder = flashOrderOptional.get();
        if (!flashOrder.getUserId().equals(userId)) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        if (FlashOrderStatus.isCanceled(flashOrder.getStatus())) {
            return false;
        }
        flashOrder.setStatus(FlashOrderStatus.CANCELED.getCode());
        boolean saveSuccess = flashOrderRepository.updateStatusForOrder(flashOrder);
        if (saveSuccess) {
            FlashOrderEvent flashOrderEvent = new FlashOrderEvent();
            flashOrderEvent.setFlashOrderEventType(FlashOrderEventType.CANCEL);
            domainEventPublisher.publish(flashOrderEvent);
        }
        log.info("cancelOrder DONE: {},{}", userId, orderId);
        return saveSuccess;
    }
}
