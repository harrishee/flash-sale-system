package com.harris.domain.service.impl;

import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.enums.SaleItemEventType;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.model.event.SaleItemEvent;
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
        if (itemId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        
        // 从仓库中获取商品
        Optional<SaleItem> optionalSaleItem = saleItemRepository.findItemById(itemId);
        if (!optionalSaleItem.isPresent()) throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        return optionalSaleItem.get();
    }
    
    @Override
    public PageResult<SaleItem> getItems(PageQuery pageQuery) {
        if (pageQuery == null) pageQuery = new PageQuery();
        
        // 从仓库中获取 商品列表 和 商品数量，包装成分页结果并返回
        List<SaleItem> saleItems = saleItemRepository.findAllItemByCondition(pageQuery.validateParams());
        Integer total = saleItemRepository.countAllItemByCondition(pageQuery);
        return PageResult.of(saleItems, total);
    }
    
    @Override
    public void publishItem(SaleItem saleItem) {
        if (saleItem == null || saleItem.invalidParams()) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        log.info("领域层服务 publishItem: [itemId={}]", saleItem.getId());
        
        // 设置状态为已发布，并保存商品到仓库
        saleItem.setStatus(SaleItemStatus.ONLINE.getCode());
        saleItemRepository.saveItem(saleItem);
        // log.info("领域层服务 publishItem, 1. 商品已保存到仓库: [itemId={}]", saleItem.getId());
        
        // 创建商品发布事件
        SaleItemEvent saleItemEvent = new SaleItemEvent();
        saleItemEvent.setSaleItemEventType(SaleItemEventType.PUBLISHED);
        saleItemEvent.setSaleItem(saleItem);
        
        // 发布商品发布事件
        domainEventPublisher.publish(saleItemEvent);
        // log.info("领域层服务 publishItem, 2. 商品发布事件发布成功: [saleItemEvent={}]", saleItemEvent);
    }
    
    @Override
    public void onlineItem(Long itemId) {
        if (itemId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        log.info("领域层 onlineItem: [itemId={}]", itemId);
        
        // 从仓库中获取商品
        Optional<SaleItem> optionalSaleItem = saleItemRepository.findItemById(itemId);
        if (!optionalSaleItem.isPresent()) throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        SaleItem saleItem = optionalSaleItem.get();
        
        // 如果商品已经上线，则直接返回
        if (SaleItemStatus.isOnline(saleItem.getStatus())) {
            log.info("领域层 onlineItem, 商品已经上线，本次操作无效");
            return;
        }
        
        // 设置状态为上线，并保存商品到仓库
        saleItem.setStatus(SaleItemStatus.ONLINE.getCode());
        saleItemRepository.saveItem(saleItem);
        // log.info("领域层 onlineItem, 1. 商品已更新上线到仓库: [itemId={}]", itemId);
        
        // 创建商品上线事件
        SaleItemEvent saleItemEvent = new SaleItemEvent();
        saleItemEvent.setSaleItemEventType(SaleItemEventType.ONLINE);
        saleItemEvent.setSaleItem(saleItem);
        
        // 发布商品上线事件
        domainEventPublisher.publish(saleItemEvent);
        // log.info("领域层 onlineItem, 2. 商品上线事件发布成功: [saleItemEvent={}]", saleItemEvent);
    }
    
    @Override
    public void offlineItem(Long itemId) {
        if (itemId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        log.info("领域层 offlineItem: [itemId={}]", itemId);
        
        // 从仓库中获取商品
        Optional<SaleItem> optionalSaleItem = saleItemRepository.findItemById(itemId);
        if (!optionalSaleItem.isPresent()) throw new DomainException(DomainErrorCode.ITEM_DOES_NOT_EXIST);
        SaleItem saleItem = optionalSaleItem.get();
        
        // 如果商品已经下线，则直接返回
        if (SaleItemStatus.isOffline(saleItem.getStatus())) {
            log.info("领域层 offlineItem, 商品已经下线，本次操作无效");
            return;
        }
        
        // 如果商品不是上线状态，抛出异常
        if (!SaleItemStatus.isOnline(saleItem.getStatus())) {
            log.info("领域层 offlineItem, 商品不是上线状态，本次操作无效");
            throw new DomainException(DomainErrorCode.ITEM_NOT_ONLINE);
        }
        
        // 设置状态为下线，并保存商品到仓库
        saleItem.setStatus(SaleItemStatus.OFFLINE.getCode());
        saleItemRepository.saveItem(saleItem);
        // log.info("领域层 offlineItem, 1. 商品已更新下线到仓库: [itemId={}]", itemId);
        
        // 创建商品下线事件
        SaleItemEvent saleItemEvent = new SaleItemEvent();
        saleItemEvent.setSaleItemEventType(SaleItemEventType.OFFLINE);
        saleItemEvent.setSaleItem(saleItem);
        
        // 发布商品下线事件
        domainEventPublisher.publish(saleItemEvent);
        // log.info("领域层 offlineItem, 2. 商品下线事件发布成功: [saleItemEvent={}]", saleItemEvent);
    }
}
