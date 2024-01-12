package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.model.enums.FlashItemStatus;
import com.harris.domain.event.flashItem.FlashItemEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.event.flashItem.FlashItemEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.repository.FlashItemRepository;
import com.harris.domain.service.FlashItemDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FlashItemDomainServiceImpl implements FlashItemDomainService {
    @Resource
    private FlashItemRepository flashItemRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public FlashItem getItem(Long itemId) {
        if (itemId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashItem> flashItemOptional = flashItemRepository.findItemById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        return flashItemOptional.get();
    }

    @Override
    public PageResult<FlashItem> getItems(PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashItem> flashItems = flashItemRepository.findItemsByCondition(pagesQueryCondition.validateParams());
        Integer total = flashItemRepository.countItemsByCondition(pagesQueryCondition);
        return PageResult.with(flashItems, total);
    }

    @Override
    public void publishItem(FlashItem flashItem) {
        log.info("publishItem TRY: {}", JSON.toJSON(flashItem));
        if (flashItem == null || flashItem.invalidParams()) {
            throw new DomainException(DomainErrCode.ONLINE_ITEM_INVALID_PARAMS);
        }
        flashItem.setStatus(FlashItemStatus.PUBLISHED.getCode());
        flashItemRepository.saveItem(flashItem);
        log.info("publishItem, item published: {}", flashItem.getId());

        FlashItemEvent flashItemEvent = new FlashItemEvent();
        flashItemEvent.setFlashItemEventType(FlashItemEventType.PUBLISHED);
        flashItemEvent.setFlashItem(flashItem);
        domainEventPublisher.publish(flashItemEvent);
        log.info("publishItem, item publish event DONE: {}", flashItem.getId());
    }

    @Override
    public void onlineItem(Long itemId) {
        log.info("onlineItem TRY: {}", itemId);
        if (itemId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashItem> flashItemOptional = flashItemRepository.findItemById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        FlashItem flashItem = flashItemOptional.get();
        if (FlashItemStatus.isOnline(flashItem.getStatus())) {
            return;
        }
        flashItem.setStatus(FlashItemStatus.ONLINE.getCode());
        flashItemRepository.saveItem(flashItem);
        log.info("onlineItem, item online: {}", itemId);

        FlashItemEvent flashItemPublishEvent = new FlashItemEvent();
        flashItemPublishEvent.setFlashItemEventType(FlashItemEventType.ONLINE);
        flashItemPublishEvent.setFlashItem(flashItem);
        domainEventPublisher.publish(flashItemPublishEvent);
        log.info("onlineItem, item online event DONE: {}", itemId);
    }

    @Override
    public void offlineItem(Long itemId) {
        log.info("offlineItem TRY: {}", itemId);
        if (itemId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashItem> flashItemOptional = flashItemRepository.findItemById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ITEM_DOES_NOT_EXIST);
        }
        FlashItem flashItem = flashItemOptional.get();
        if (FlashItemStatus.isOffline(flashItem.getStatus())) {
            return;
        }
        flashItem.setStatus(FlashItemStatus.OFFLINE.getCode());
        flashItemRepository.saveItem(flashItem);
        log.info("offlineItem, item offline: {}", itemId);

        FlashItemEvent flashItemEvent = new FlashItemEvent();
        flashItemEvent.setFlashItemEventType(FlashItemEventType.OFFLINE);
        flashItemEvent.setFlashItem(flashItem);
        domainEventPublisher.publish(flashItemEvent);
        log.info("offlineItem, item offline event DONE: {}", itemId);
    }
}
