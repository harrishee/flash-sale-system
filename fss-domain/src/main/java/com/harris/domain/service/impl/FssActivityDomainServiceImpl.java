package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.model.enums.SaleActivityStatus;
import com.harris.domain.event.flashActivity.FlashActivityEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.event.flashActivity.FlashActivityEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.repository.SaleActivityRepository;
import com.harris.domain.service.FssActivityDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FssActivityDomainServiceImpl implements FssActivityDomainService {
    @Resource
    private SaleActivityRepository saleActivityRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SaleActivity getActivity(Long activityId) {
        if (activityId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleActivity> flashActivity = saleActivityRepository.findActivityById(activityId);
        return flashActivity.orElse(null);
    }

    @Override
    public PageResult<SaleActivity> getActivities(PageQueryCondition pageQueryCondition) {
        if (pageQueryCondition == null) {
            pageQueryCondition = new PageQueryCondition();
        }
        List<SaleActivity> flashActivities = saleActivityRepository.findActivitiesByCondition(pageQueryCondition.validateParams());
        Integer total = saleActivityRepository.countActivitiesByCondition(pageQueryCondition);
        return PageResult.with(flashActivities, total);
    }

    @Override
    public void publishActivity(Long userId, SaleActivity saleActivity) {
        log.info("publishActivity TRY: {},{}", userId, JSON.toJSONString(saleActivity));
        if (saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }
        saleActivity.setStatus(SaleActivityStatus.PUBLISHED.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("publishActivity, activity published: {},{}", userId, saleActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.PUBLISHED);
        flashActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("publishActivity, activity publish event DONE: {}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void modifyActivity(Long userId, SaleActivity saleActivity) {
        log.info("modifyActivity TRY: {},{}", userId, JSON.toJSONString(saleActivity));
        if (saleActivity == null || saleActivity.invalidParams()) {
            throw new DomainException(DomainErrCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }
        saleActivityRepository.saveActivity(saleActivity);
        log.info("modifyActivity, activity modified: {},{}", userId, saleActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.MODIFIED);
        flashActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("modifyActivity, activity modified event DONE: {}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void onlineActivity(Long userId, Long activityId) {
        log.info("onlineActivity TRY: {},{}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleActivity> flashActivityOptional = saleActivityRepository.findActivityById(activityId);
        if (!flashActivityOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ACTIVITY_DOES_NOT_EXIST);
        }
        SaleActivity saleActivity = flashActivityOptional.get();
        if (SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            return;
        }
        saleActivity.setStatus(SaleActivityStatus.ONLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("onlineActivity, activity online: {},{}", userId, saleActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.ONLINE);
        flashActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("onlineActivity, activity online event DONE: {}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void offlineActivity(Long userId, Long activityId) {
        log.info("offlineActivity|下线秒杀活动|{},{}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<SaleActivity> flashActivityOptional = saleActivityRepository.findActivityById(activityId);
        if (!flashActivityOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ACTIVITY_DOES_NOT_EXIST);
        }
        SaleActivity saleActivity = flashActivityOptional.get();
        if (SaleActivityStatus.isOffline(saleActivity.getStatus())) {
            return;
        }
        if (!SaleActivityStatus.isOnline(saleActivity.getStatus())) {
            throw new DomainException(DomainErrCode.ACTIVITY_NOT_ONLINE);
        }
        saleActivity.setStatus(SaleActivityStatus.OFFLINE.getCode());
        saleActivityRepository.saveActivity(saleActivity);
        log.info("offlineActivity, activity offline: {},{}", userId, saleActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.OFFLINE);
        flashActivityEvent.setSaleActivity(saleActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("offlineActivity, activity offline event DONE: {}", JSON.toJSON(flashActivityEvent));
    }
}
