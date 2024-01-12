package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.model.enums.FlashActivityStatus;
import com.harris.domain.event.flashActivity.FlashActivityEventType;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.event.flashActivity.FlashActivityEvent;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashActivity;
import com.harris.domain.repository.FlashActivityRepository;
import com.harris.domain.service.FlashActivityDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FlashActivityDomainServiceImpl implements FlashActivityDomainService {
    @Resource
    private FlashActivityRepository flashActivityRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public FlashActivity getActivity(Long activityId) {
        if (activityId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashActivity> flashActivity = flashActivityRepository.findActivityById(activityId);
        return flashActivity.orElse(null);
    }

    @Override
    public PageResult<FlashActivity> getActivities(PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashActivity> flashActivities = flashActivityRepository.findActivitiesByCondition(pagesQueryCondition.validateParams());
        Integer total = flashActivityRepository.countActivitiesByCondition(pagesQueryCondition);
        return PageResult.with(flashActivities, total);
    }

    @Override
    public void publishActivity(Long userId, FlashActivity flashActivity) {
        log.info("publishActivity TRY: {},{}", userId, JSON.toJSONString(flashActivity));
        if (flashActivity == null || flashActivity.invalidParams()) {
            throw new DomainException(DomainErrCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }
        flashActivity.setStatus(FlashActivityStatus.PUBLISHED.getCode());
        flashActivityRepository.saveActivity(flashActivity);
        log.info("publishActivity, activity published: {},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.PUBLISHED);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("publishActivity, activity publish event DONE: {}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void modifyActivity(Long userId, FlashActivity flashActivity) {
        log.info("modifyActivity TRY: {},{}", userId, JSON.toJSONString(flashActivity));
        if (flashActivity == null || flashActivity.invalidParams()) {
            throw new DomainException(DomainErrCode.ONLINE_ACTIVITY_INVALID_PARAMS);
        }
        flashActivityRepository.saveActivity(flashActivity);
        log.info("modifyActivity, activity modified: {},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.MODIFIED);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("modifyActivity, activity modified event DONE: {}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void onlineActivity(Long userId, Long activityId) {
        log.info("onlineActivity TRY: {},{}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashActivity> flashActivityOptional = flashActivityRepository.findActivityById(activityId);
        if (!flashActivityOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ACTIVITY_DOES_NOT_EXIST);
        }
        FlashActivity flashActivity = flashActivityOptional.get();
        if (FlashActivityStatus.isOnline(flashActivity.getStatus())) {
            return;
        }
        flashActivity.setStatus(FlashActivityStatus.ONLINE.getCode());
        flashActivityRepository.saveActivity(flashActivity);
        log.info("onlineActivity, activity online: {},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.ONLINE);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("onlineActivity, activity online event DONE: {}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void offlineActivity(Long userId, Long activityId) {
        log.info("offlineActivity|下线秒杀活动|{},{}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        Optional<FlashActivity> flashActivityOptional = flashActivityRepository.findActivityById(activityId);
        if (!flashActivityOptional.isPresent()) {
            throw new DomainException(DomainErrCode.ACTIVITY_DOES_NOT_EXIST);
        }
        FlashActivity flashActivity = flashActivityOptional.get();
        if (FlashActivityStatus.isOffline(flashActivity.getStatus())) {
            return;
        }
        if (!FlashActivityStatus.isOnline(flashActivity.getStatus())) {
            throw new DomainException(DomainErrCode.ACTIVITY_NOT_ONLINE);
        }
        flashActivity.setStatus(FlashActivityStatus.OFFLINE.getCode());
        flashActivityRepository.saveActivity(flashActivity);
        log.info("offlineActivity, activity offline: {},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setFlashActivityEventType(FlashActivityEventType.OFFLINE);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        log.info("offlineActivity, activity offline event DONE: {}", JSON.toJSON(flashActivityEvent));
    }
}
