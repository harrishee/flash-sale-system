package com.harris.domain.service.impl;

import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.enums.SaleActivityEventType;
import com.harris.domain.model.enums.SaleActivityStatus;
import com.harris.domain.model.event.SaleActivityEvent;
import com.harris.domain.repository.SaleActivityRepository;
import com.harris.domain.service.SaleActivityDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SaleActivityDomainServiceImpl implements SaleActivityDomainService {
    @Resource
    private SaleActivityRepository saleActivityRepository;
    
    @Resource
    private DomainEventPublisher domainEventPublisher;
    
    @Override
    public SaleActivity getActivity(Long activityId) {
        if (activityId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        
        // 从仓库中获取活动
        Optional<SaleActivity> saleActivity = saleActivityRepository.findActivityById(activityId);
        if (!saleActivity.isPresent()) throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        return saleActivity.get();
    }
    
    @Override
    public PageResult<SaleActivity> getActivities(PageQuery pageQuery) {
        if (pageQuery == null) pageQuery = new PageQuery();
        
        // 从仓库中获取 活动列表 和 活动数量，包装成分页结果并返回
        List<SaleActivity> saleActivities = saleActivityRepository.findAllActivityByCondition(pageQuery.validateParams());
        Integer total = saleActivityRepository.countAllActivityByCondition(pageQuery);
        return PageResult.of(saleActivities, total);
    }
    
    @Override
    public void publishActivity(Long userId, SaleActivity saleActivity) {
        if (userId == null || saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        log.info("领域层服务 publishActivity: [userId={}, activityId={}]", userId, saleActivity.getId());
        
        // 设置状态为已发布，并保存活动到仓库
        saleActivity.setStatus(SaleActivityStatus.PUBLISHED.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("领域层服务 publishActivity, 1. 活动已保存到仓库: [userId={}, activityId={}]", userId, saleActivity.getId());
        
        // 创建活动发布事件
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.PUBLISHED);
        saleActivityEvent.setSaleActivity(saleActivity);
        
        // 发布活动发布事件
        domainEventPublisher.publish(saleActivityEvent);
        log.info("领域层服务 publishActivity, 2. 活动发布事件已发布: [saleActivityEvent={}]", saleActivityEvent);
    }
    
    @Override
    public void modifyActivity(Long userId, SaleActivity saleActivity) {
        if (saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        log.info("领域层服务 modifyActivity: [userId={}, activityId={}]", userId, saleActivity.getId());
        
        // 更新活动到仓库
        saleActivityRepository.saveActivity(saleActivity);
        log.info("领域层服务 modifyActivity, 1. 活动已更新到仓库: [userId={}, activityId={}]", userId, saleActivity.getId());
        
        // 创建活动修改事件
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.MODIFIED);
        saleActivityEvent.setSaleActivity(saleActivity);
        
        // 发布活动修改事件
        domainEventPublisher.publish(saleActivityEvent);
        log.info("领域层服务 modifyActivity, 2. 活动修改事件已发布: [saleActivityEvent={}]", saleActivityEvent);
    }
    
    @Override
    public void onlineActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        log.info("领域层服务 onlineActivity: [userId={}, activityId={}]", userId, activityId);
        
        // 从仓库中获取活动
        Optional<SaleActivity> optionalSaleActivity = saleActivityRepository.findActivityById(activityId);
        if (!optionalSaleActivity.isPresent()) throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        
        // 如果活动已经上线，直接返回
        SaleActivity saleActivity = optionalSaleActivity.get();
        if (SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            log.info("领域层服务 onlineActivity, 活动已上线，本次操作无效");
            return;
        }
        
        // 设置状态为上线，并保存活动到仓库
        saleActivity.setStatus(SaleActivityStatus.ONLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("领域层服务 onlineActivity, 1. 活动已更新上线到仓库: [userId={}, activityId={}]", userId, activityId);
        
        // 创建活动上线事件
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.ONLINE);
        saleActivityEvent.setSaleActivity(saleActivity);
        
        // 发布活动上线事件
        domainEventPublisher.publish(saleActivityEvent);
        log.info("领域层 onlineActivity, 2. 活动上线事件已发布: [saleActivityEvent={}]", saleActivityEvent);
    }
    
    @Override
    public void offlineActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null) throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        log.info("领域层 offlineActivity: [userId={}, activityId={}]", userId, activityId);
        
        // 从仓库中获取活动
        Optional<SaleActivity> optionalSaleActivity = saleActivityRepository.findActivityById(activityId);
        if (!optionalSaleActivity.isPresent()) throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        
        // 如果活动已经下线，直接返回
        SaleActivity saleActivity = optionalSaleActivity.get();
        if (SaleActivityStatus.isOffline(saleActivity.getStatus())) {
            log.info("领域层 offlineActivity, 活动已下线，本次操作无效");
            return;
        }
        
        // 如果活动不是上线状态，抛出异常
        if (!SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            log.info("领域层 offlineActivity, 活动不是上线状态，本次操作无效");
            throw new DomainException(DomainErrorCode.ACTIVITY_NOT_ONLINE);
        }
        
        // 设置状态为下线，并保存活动到仓库
        saleActivity.setStatus(SaleActivityStatus.OFFLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("领域层 offlineActivity, 1. 活动已更新下线到仓库: [userId={}, activityId={}]", userId, activityId);
        
        // 创建活动下线事件
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.OFFLINE);
        saleActivityEvent.setSaleActivity(saleActivity);
        
        // 发布活动下线事件
        domainEventPublisher.publish(saleActivityEvent);
        log.info("领域层 offlineActivity, 2. 活动下线事件已发布: [saleActivityEvent={}]", saleActivityEvent);
    }
}
