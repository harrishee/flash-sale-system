package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.model.enums.SaleItemEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.model.event.SaleItemEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.repository.SaleItemRepository;
import com.harris.domain.service.SaleItemDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SaleItemDomainServiceImpl implements SaleItemDomainService {
    @Resource
    private SaleItemRepository saleItemRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SaleItem getItem(Long itemId) {
        // Validate params
        if (itemId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find item from repository and validate
        Optional<SaleItem> optionalSaleItem = saleItemRepository.findItemById(itemId);
        if (!optionalSaleItem.isPresent()) {
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }
        return optionalSaleItem.get();
    }

    @Override
    public PageResult<SaleItem> getItems(PageQuery pageQuery) {
        pageQuery = pageQuery == null ? new PageQuery() : pageQuery;
        pageQuery.validateParams();

        // Find items with condition
        List<SaleItem> saleItems = saleItemRepository.findItemsByCondition(pageQuery);
        Integer total = saleItemRepository.countItemsByCondition(pageQuery);
        return PageResult.of(saleItems, total);
    }

    @Override
    public void publishItem(SaleItem saleItem) {
        log.info("publishItem: {}", JSON.toJSON(saleItem));

        // Validate params
        if (saleItem == null || saleItem.invalidParams()) {
            throw new DomainException(DomainErrorCode.ONLINE_ITEM_INVALID_PARAMS);
        }

        // Set status to published and save item to repository
        saleItem.setStatus(SaleItemStatus.PUBLISHED.getCode());
        saleItemRepository.saveItem(saleItem);
        log.info("publishItem, item published: {}", saleItem.getId());

        // Publish the event
        SaleItemEvent saleItemEvent = new SaleItemEvent();
        saleItemEvent.setSaleItemEventType(SaleItemEventType.PUBLISHED);
        saleItemEvent.setSaleItem(saleItem);
        domainEventPublisher.publish(saleItemEvent);
        log.info("publishItem, item publish event published: {}", saleItem.getId());
    }

    @Override
    public void onlineItem(Long itemId) {
        log.info("onlineItem: {}", itemId);

        // Validate params
        if (itemId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find item from repository and validate
        Optional<SaleItem> optionalSaleItem = saleItemRepository.findItemById(itemId);
        if (!optionalSaleItem.isPresent()) {
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }
        SaleItem saleItem = optionalSaleItem.get();

        // Return if item is already online
        if (SaleItemStatus.isOnline(saleItem.getStatus())) {
            return;
        }

        // Set status to online and save item to repository
        saleItem.setStatus(SaleItemStatus.ONLINE.getCode());
        saleItemRepository.saveItem(saleItem);
        log.info("onlineItem, item online: {}", itemId);

        // Publish the event
        SaleItemEvent saleItemEvent = new SaleItemEvent();
        saleItemEvent.setSaleItemEventType(SaleItemEventType.ONLINE);
        saleItemEvent.setSaleItem(saleItem);
        domainEventPublisher.publish(saleItemEvent);
        log.info("onlineItem, item online event published: {}", itemId);
    }

    @Override
    public void offlineItem(Long itemId) {
        log.info("offlineItem TRY: {}", itemId);

        // Validate params
        if (itemId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find item from repository and validate
        Optional<SaleItem> optionalSaleItem = saleItemRepository.findItemById(itemId);
        if (!optionalSaleItem.isPresent()) {
            throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        }
        SaleItem saleItem = optionalSaleItem.get();

        // Return if item is already offline
        if (SaleItemStatus.isOffline(saleItem.getStatus())) {
            return;
        }

        // Check if item is not online yet
        if (!SaleItemStatus.isOnline(saleItem.getStatus())) {
            throw new DomainException(DomainErrorCode.ITEM_NOT_ONLINE);
        }

        // Set status to offline and save item to repository
        saleItem.setStatus(SaleItemStatus.OFFLINE.getCode());
        saleItemRepository.saveItem(saleItem);
        log.info("offlineItem, item offline: {}", itemId);

        // Publish the event
        SaleItemEvent saleItemEvent = new SaleItemEvent();
        saleItemEvent.setSaleItemEventType(SaleItemEventType.OFFLINE);
        saleItemEvent.setSaleItem(saleItem);
        domainEventPublisher.publish(saleItemEvent);
        log.info("offlineItem, item offline event published: {}", itemId);
    }
}
