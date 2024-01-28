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
    public PageResult<SaleActivity> getActivities(PageQuery pageQuery) {
        pageQuery = pageQuery == null ? new PageQuery() : pageQuery;
        pageQuery.validateParams();

        // Get activities from repository
        List<SaleActivity> saleActivities = saleActivityRepository.findActivitiesByCondition(pageQuery);
        Integer total = saleActivityRepository.countActivitiesByCondition(pageQuery);
        return PageResult.of(saleActivities, total);
    }

    @Override
    public void publishActivity(Long userId, SaleActivity saleActivity) {
        log.info("domain publishActivity: {} | {}", userId, saleActivity);
        if (userId == null || saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrorCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }

        // Set status to published and save activity to repository
        saleActivity.setStatus(SaleActivityStatus.PUBLISHED.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("domain publishActivity, activity saved to repository: {} | {}", userId, saleActivity.getId());

        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.PUBLISHED);
        saleActivityEvent.setSaleActivity(saleActivity);

        // Publish the publish event
        log.info("domain publishActivity, activity publish event published: {}", saleActivityEvent);
        domainEventPublisher.publish(saleActivityEvent);
    }

    @Override
    public void modifyActivity(Long userId, SaleActivity saleActivity) {
        log.info("domain modifyActivity: {} | {}", userId, saleActivity);
        if (saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrorCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }

        // Update activity to repository
        saleActivityRepository.saveActivity(saleActivity);
        log.info("domain modifyActivity, activity updated to repository: {} | {}", userId, saleActivity.getId());

        // Publish the modification event
        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.MODIFIED);
        saleActivityEvent.setSaleActivity(saleActivity);

        log.info("domain modifyActivity, activity modification event published: {}", saleActivityEvent);
        domainEventPublisher.publish(saleActivityEvent);
    }

    @Override
    public void onlineActivity(Long userId, Long activityId) {
        log.info("domain onlineActivity: {} | {}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find activity from repository
        Optional<SaleActivity> optionalSaleActivity = saleActivityRepository.findActivityById(activityId);
        if (!optionalSaleActivity.isPresent()) {
            throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        }

        // If activity is already online
        SaleActivity saleActivity = optionalSaleActivity.get();
        if (SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            log.info("domain onlineActivity, activity already online: {} | {}", userId, activityId);
            return;
        }

        // Set status to online and update activity to repository
        saleActivity.setStatus(SaleActivityStatus.ONLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("domain onlineActivity, activity updated to repository: {} | {}", userId, activityId);

        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.ONLINE);
        saleActivityEvent.setSaleActivity(saleActivity);

        // Publish the online event
        log.info("domain onlineActivity, activity online event published: {}", saleActivityEvent);
        domainEventPublisher.publish(saleActivityEvent);
    }

    @Override
    public void offlineActivity(Long userId, Long activityId) {
        log.info("domain offlineActivity: {} | {}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Find activity from repository
        Optional<SaleActivity> optionalSaleActivity = saleActivityRepository.findActivityById(activityId);
        if (!optionalSaleActivity.isPresent()) {
            throw new DomainException(DomainErrorCode.ACTIVITY_DOES_NOT_EXIST);
        }

        // If activity is already offline
        SaleActivity saleActivity = optionalSaleActivity.get();
        if (SaleActivityStatus.isOffline(saleActivity.getStatus())) {
            log.info("domain offlineActivity, activity already offline: {} | {}", userId, activityId);
            return;
        }

        // If activity is not online yet
        if (!SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            throw new DomainException(DomainErrorCode.ACTIVITY_NOT_ONLINE);
        }

        // Set status to offline and save activity to repository
        saleActivity.setStatus(SaleActivityStatus.OFFLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("domain offlineActivity, activity updated to repository: {} | {}", userId, activityId);

        SaleActivityEvent saleActivityEvent = new SaleActivityEvent();
        saleActivityEvent.setSaleActivityEventType(SaleActivityEventType.OFFLINE);
        saleActivityEvent.setSaleActivity(saleActivity);

        // Publish the offline event
        log.info("domain offlineActivity, activity offline event published: {}", saleActivityEvent);
        domainEventPublisher.publish(saleActivityEvent);
    }
}
