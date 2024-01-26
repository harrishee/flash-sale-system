package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.event.flashItem.FlashItemEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.event.flashItem.FlashItemEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.repository.FlashItemRepository;
import com.harris.domain.service.FssItemDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FssItemDomainServiceImpl implements FssItemDomainService {
    @Resource
    private FlashItemRepository flashItemRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SaleItem getItem(Long itemId) {
        if (itemId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleItem> flashItemOptional = flashItemRepository.findItemById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        return flashItemOptional.get();
    }

    @Override
    public PageResult<SaleItem> getItems(PageQueryCondition pageQueryCondition) {
        if (pageQueryCondition == null) {
            pageQueryCondition = new PageQueryCondition();
        }
        List<SaleItem> saleItems = flashItemRepository.findItemsByCondition(pageQueryCondition.validateParams());
        Integer total = flashItemRepository.countItemsByCondition(pageQueryCondition);
        return PageResult.with(saleItems, total);
    }

    @Override
    public void publishItem(SaleItem saleItem) {
        log.info("publishItem TRY: {}", JSON.toJSON(saleItem));
        if (saleItem == null || saleItem.invalidParams()) {
            throw new DomainException(DomainErrCode.ONLINE_ITEM_INVALID_PARAMS);
        }
        saleItem.setStatus(SaleItemStatus.PUBLISHED.getCode());
        flashItemRepository.saveItem(saleItem);
        log.info("publishItem, item published: {}", saleItem.getId());

        FlashItemEvent flashItemEvent = new FlashItemEvent();
        flashItemEvent.setFlashItemEventType(FlashItemEventType.PUBLISHED);
        flashItemEvent.setSaleItem(saleItem);
        domainEventPublisher.publish(flashItemEvent);
        log.info("publishItem, item publish event DONE: {}", saleItem.getId());
    }

    @Override
    public void onlineItem(Long itemId) {
        log.info("onlineItem TRY: {}", itemId);
        if (itemId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleItem> flashItemOptional = flashItemRepository.findItemById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        SaleItem saleItem = flashItemOptional.get();
        if (SaleItemStatus.isOnline(saleItem.getStatus())) {
            return;
        }
        saleItem.setStatus(SaleItemStatus.ONLINE.getCode());
        flashItemRepository.saveItem(saleItem);
        log.info("onlineItem, item online: {}", itemId);

        FlashItemEvent flashItemPublishEvent = new FlashItemEvent();
        flashItemPublishEvent.setFlashItemEventType(FlashItemEventType.ONLINE);
        flashItemPublishEvent.setSaleItem(saleItem);
        domainEventPublisher.publish(flashItemPublishEvent);
        log.info("onlineItem, item online event DONE: {}", itemId);
    }

    @Override
    public void offlineItem(Long itemId) {
        log.info("offlineItem TRY: {}", itemId);
        if (itemId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleItem> flashItemOptional = flashItemRepository.findItemById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        SaleItem saleItem = flashItemOptional.get();
        if (SaleItemStatus.isOffline(saleItem.getStatus())) {
            return;
        }
        saleItem.setStatus(SaleItemStatus.OFFLINE.getCode());
        flashItemRepository.saveItem(saleItem);
        log.info("offlineItem, item offline: {}", itemId);

        FlashItemEvent flashItemEvent = new FlashItemEvent();
        flashItemEvent.setFlashItemEventType(FlashItemEventType.OFFLINE);
        flashItemEvent.setSaleItem(saleItem);
        domainEventPublisher.publish(flashItemEvent);
        log.info("offlineItem, item offline event DONE: {}", itemId);
    }
}
