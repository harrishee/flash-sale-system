package com.harris.app.service.app.impl;

import com.harris.app.exception.BizException;
import com.harris.app.auth.model.AuthResult;
import com.harris.app.model.cache.FlashActivitiesCache;
import com.harris.app.model.cache.FlashActivityCache;
import com.harris.app.model.command.FlashActivityPublishCommand;
import com.harris.app.model.converter.FlashActivityAppConverter;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.FlashActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.auth.AuthAppService;
import com.harris.app.service.app.FlashActivityAppService;
import com.harris.app.service.cache.FlashActivitiesCacheService;
import com.harris.app.service.cache.FlashActivityCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.FlashActivity;
import com.harris.domain.service.FlashActivityDomainService;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.harris.app.exception.AppErrCode.*;
import static com.harris.app.auth.model.ResourceEnum.ACTIVITY_CREATE;
import static com.harris.app.auth.model.ResourceEnum.ACTIVITY_MODIFICATION;
import static com.harris.infra.controller.exception.AuthErrCode.UNAUTHORIZED_ACCESS;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashActivityAppServiceImpl implements FlashActivityAppService {
    public static final String ACTIVITY_CREATE_LOCK = "ACTIVITY_LOCK";
    public static final String ACTIVITY_MODIFICATION_LOCK = "ACTIVITY_MODIFICATION_LOCK";

    @Resource
    private AuthAppService authAppService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Resource
    private FlashActivityCacheService flashActivityCacheService;

    @Resource
    private FlashActivitiesCacheService flashActivitiesCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    public AppSingleResult<SaleActivityDTO> getFlashActivity(Long userId, Long activityId, Long version) {
        if (userId == null || activityId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        FlashActivityCache flashActivityCache = flashActivityCacheService.getActivityCache(activityId, version);
        if (!flashActivityCache.isExist()) {
            throw new BizException(ACTIVITY_NOT_FOUND.getErrDesc());
        }
        if (flashActivityCache.isLater()) {
            return AppSingleResult.tryLater();
        }
        SaleActivityDTO saleActivityDTO = FlashActivityAppConverter.toDTO(flashActivityCache.getFlashActivity());
        saleActivityDTO.setVersion(flashActivityCache.getVersion());
        return AppSingleResult.ok(saleActivityDTO);
    }

    @Override
    public AppMultiResult<SaleActivityDTO> getFlashActivities(Long userId, FlashActivitiesQuery flashActivitiesQuery) {
        List<FlashActivity> activities;
        Integer total;
        if (flashActivitiesQuery.isFirstPageQuery()) {
            FlashActivitiesCache flashActivitiesCache = flashActivitiesCacheService.getActivitiesCache(flashActivitiesQuery.getPageNumber(), flashActivitiesQuery.getVersion());
            if (flashActivitiesCache.isLater()) {
                return AppMultiResult.tryLater();
            }
            activities = flashActivitiesCache.getFlashActivities();
            total = flashActivitiesCache.getTotal();
        } else {
            PageResult<FlashActivity> flashActivityPageResult = flashActivityDomainService.getActivities(FlashActivityAppConverter.toQuery(flashActivitiesQuery));
            activities = flashActivityPageResult.getData();
            total = flashActivityPageResult.getTotal();
        }
        List<SaleActivityDTO> saleActivityDTOS = activities.stream().map(FlashActivityAppConverter::toDTO).collect(Collectors.toList());
        return AppMultiResult.of(total, saleActivityDTOS);
    }

    @Override
    public AppResult publishFlashActivity(Long userId, FlashActivityPublishCommand flashActivityPublishCommand) {
        if (userId == null || flashActivityPublishCommand == null || flashActivityPublishCommand.invalidParams()) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ACTIVITY_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock activityCreateLock = distributedLockService.getDistributedLock(buildActivityCreateKey(userId));
        try {
            boolean isLocked = activityCreateLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(FREQUENTLY_ERROR);
            }
            flashActivityDomainService.publishActivity(userId, FlashActivityAppConverter.toDomainObj(flashActivityPublishCommand));
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("publishFlashActivity failed");
        } finally {
            activityCreateLock.unlock();
        }
    }

    @Override
    public AppResult modifyFlashActivity(Long userId, Long activityId, FlashActivityPublishCommand flashActivityPublishCommand) {
        if (userId == null || flashActivityPublishCommand == null || flashActivityPublishCommand.invalidParams()) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock activityModificationLock = distributedLockService.getDistributedLock(buildActivityModificationKey(activityId));
        try {
            boolean isLocked = activityModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(FREQUENTLY_ERROR);
            }
            FlashActivity flashActivity = FlashActivityAppConverter.toDomainObj(flashActivityPublishCommand);
            flashActivity.setId(activityId);
            flashActivityDomainService.modifyActivity(userId, flashActivity);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("modifyFlashActivity failed");
        } finally {
            activityModificationLock.unlock();
        }
    }

    @Override
    public AppResult onlineFlashActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ACTIVITY_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock activityModificationLock = distributedLockService.getDistributedLock(buildActivityModificationKey(activityId));
        try {
            boolean isLocked = activityModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(FREQUENTLY_ERROR);
            }
            flashActivityDomainService.onlineActivity(userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("onlineFlashActivity failed");
        } finally {
            activityModificationLock.unlock();
        }
    }

    @Override
    public AppResult offlineFlashActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock activityModificationLock = distributedLockService.getDistributedLock(buildActivityModificationKey(activityId));
        try {
            boolean isLockSuccess = activityModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLockSuccess) {
                throw new BizException(FREQUENTLY_ERROR);
            }
            flashActivityDomainService.offlineActivity(userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("offlineFlashActivity failed");
        } finally {
            activityModificationLock.unlock();
        }
    }

    @Override
    public boolean isPlaceOrderAllowed(Long activityId) {
        FlashActivityCache flashActivityCache = flashActivityCacheService.getActivityCache(activityId, null);
        if (flashActivityCache.isLater()) {
            log.info("isPlaceOrderAllowed, try later: {}", activityId);
            return false;
        }
        if (!flashActivityCache.isExist() || flashActivityCache.getFlashActivity() == null) {
            log.info("isPlaceOrderAllowed, activity not exist: {}", activityId);
            return false;
        }
        FlashActivity flashActivity = flashActivityCache.getFlashActivity();
        if (!flashActivity.isOnline()) {
            log.info("isPlaceOrderAllowed, activity not online: {}", activityId);
            return false;
        }
        if (!flashActivity.isInProgress()) {
            log.info("isPlaceOrderAllowed, activity not in progress: {}", activityId);
            return false;
        }
        return true;
    }

    private String buildActivityCreateKey(Long userId) {
        return link(ACTIVITY_CREATE_LOCK, userId);
    }

    private String buildActivityModificationKey(Long activityId) {
        return link(ACTIVITY_MODIFICATION_LOCK, activityId);
    }
}
