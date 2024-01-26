package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.model.enums.SaleActivityStatus;
import com.harris.domain.model.enums.SaleActivityEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.model.event.SaleActivityEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;
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
        // Validate params
        if (activityId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find activity from repository and validate
        Optional<SaleActivity> saleActivity = saleActivityRepository.findActivityById(activityId);
        if (!saleActivity.isPresent()) {
            throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        }
        return saleActivity.get();
    }

    @Override
    public PageResult<SaleActivity> getActivities(PageQueryCondition pageQueryCondition) {
        // Validate params, set default value if necessary
        if (pageQueryCondition == null) {
            pageQueryCondition = new PageQueryCondition();
            pageQueryCondition.validateParams();
        }

        // Find activities with condition
        List<SaleActivity> saleActivities = saleActivityRepository.findActivitiesByCondition(pageQueryCondition);
        Integer total = saleActivityRepository.countActivitiesByCondition(pageQueryCondition);
        return PageResult.with(saleActivities, total);
    }

    @Override
    public void publishActivity(Long userId, SaleActivity saleActivity) {
        log.info("publishActivity: {},{}", userId, JSON.toJSONString(saleActivity));

        // Validate params
        if (saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrorCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }

        // Set status to published and save activity to repository
        saleActivity.setStatus(SaleActivityStatus.PUBLISHED.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("publishActivity, activity published: {},{}", userId, saleActivity.getId());

        // Publish the event
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.PUBLISHED);
        saleActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(saleActivityEvent);
        log.info("publishActivity, activity publish event published: {}", JSON.toJSON(saleActivityEvent));
    }

    @Override
    public void modifyActivity(Long userId, SaleActivity saleActivity) {
        log.info("modifyActivity: {},{}", userId, JSON.toJSONString(saleActivity));

        // Validate params
        if (saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrorCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }

        // Save activity to repository
        saleActivityRepository.saveActivity(saleActivity);
        log.info("modifyActivity, activity modified: {},{}", userId, saleActivity.getId());

        // Publish the event
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.MODIFIED);
        saleActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(saleActivityEvent);
        log.info("modifyActivity, activity modification event published: {}", JSON.toJSON(saleActivityEvent));
    }

    @Override
    public void onlineActivity(Long userId, Long activityId) {
        log.info("onlineActivity: {},{}", userId, activityId);

        // Validate params
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find activity from repository and validate
        Optional<SaleActivity> optionalSaleActivity = saleActivityRepository.findActivityById(activityId);
        if (!optionalSaleActivity.isPresent()) {
            throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        }
        SaleActivity saleActivity = optionalSaleActivity.get();

        // Return if activity is already online
        if (SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            return;
        }

        // Set status to online and save activity to repository
        saleActivity.setStatus(SaleActivityStatus.ONLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("onlineActivity, activity online: {},{}", userId, saleActivity.getId());

        // Publish the event
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.ONLINE);
        saleActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(saleActivityEvent);
        log.info("onlineActivity, activity online event published: {}", JSON.toJSON(saleActivityEvent));
    }

    @Override
    public void offlineActivity(Long userId, Long activityId) {
        log.info("offlineActivity: {},{}", userId, activityId);

        // Validate params
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find activity from repository and validate
        Optional<SaleActivity> optionalSaleActivity = saleActivityRepository.findActivityById(activityId);
        if (!optionalSaleActivity.isPresent()) {
            throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        }
        SaleActivity saleActivity = optionalSaleActivity.get();

        // Return if activity is already offline
        if (SaleActivityStatus.isOffline(saleActivity.getStatus())) {
            return;
        }

        // Check if activity is not online yet
        if (!SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            throw new DomainException(DomainErrorCode.ACTIVITY_NOT_ONLINE);
        }

        // Set status to offline and save activity to repository
        saleActivity.setStatus(SaleActivityStatus.OFFLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("offlineActivity, activity offline: {},{}", userId, saleActivity.getId());

        // Publish the event
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.OFFLINE);
        saleActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(saleActivityEvent);
        log.info("offlineActivity, activity offline event published: {}", JSON.toJSON(saleActivityEvent));
    }
}
