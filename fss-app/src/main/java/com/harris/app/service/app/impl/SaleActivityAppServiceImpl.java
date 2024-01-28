package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.auth.ResourceEnum;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.converter.SaleActivityAppConverter;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.AuthAppService;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.app.service.cache.SaleActivitiesCacheService;
import com.harris.app.service.cache.SaleActivityCacheService;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.enums.SaleActivityStatus;
import com.harris.domain.service.SaleActivityDomainService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import com.harris.infra.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaleActivityAppServiceImpl implements SaleActivityAppService {
    public static final String ACTIVITY_CREATE_LOCK = "ACTIVITY_CREATE_LOCK";
    public static final String ACTIVITY_MODIFICATION_LOCK = "ACTIVITY_MODIFICATION_LOCK";

    @Resource
    private AuthAppService authAppService;

    @Resource
    private SaleActivityDomainService saleActivityDomainService;

    @Resource
    private SaleActivityCacheService saleActivityCacheService;

    @Resource
    private SaleActivitiesCacheService saleActivitiesCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    public AppSingleResult<SaleActivityDTO> getActivity(Long userId, Long activityId, Long version) {
        log.info("getActivity: {} | {} | {}", userId, activityId, version);
        if (userId == null || activityId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Retrieve activity from cache and check if it exists
        SaleActivityCache activityCache = saleActivityCacheService.getActivityCache(activityId, version);
        if (!activityCache.isExist()) {
            throw new BizException(AppErrorCode.ACTIVITY_NOT_FOUND.getErrDesc());
        }

        if (activityCache.isLater()) {
            log.info("getActivity tryLater: {} | {} | {}", userId, activityId, version);
            return AppSingleResult.tryLater();
        }

        SaleActivity saleActivity = activityCache.getSaleActivity();
        SaleActivityDTO saleActivityDTO = SaleActivityAppConverter.toDTO(saleActivity);
        saleActivityDTO.setVersion(activityCache.getVersion());

        log.info("getActivity ok: {} | {} | {}", userId, activityId, version);
        return AppSingleResult.ok(saleActivityDTO);
    }

    @Override
    public AppMultiResult<SaleActivityDTO> listActivities(Long userId, SaleActivitiesQuery saleActivitiesQuery) {
        log.info("listActivities: {} | {}", userId, saleActivitiesQuery);
        if (saleActivitiesQuery == null) {
            return AppMultiResult.empty();
        }

        List<SaleActivity> activities;
        Integer total;

        // If it's the default first page query, and not online query, get values from cache
        // otherwise, get values from domain service
        if (saleActivitiesQuery.isFirstPageQuery() &&
                !Objects.equals(SaleActivityStatus.ONLINE.getCode(), saleActivitiesQuery.getStatus())) {

            Integer pageNumber = saleActivitiesQuery.getPageNumber();
            Long version = saleActivitiesQuery.getVersion();

            // Get activities from cache
            SaleActivitiesCache activitiesCache = saleActivitiesCacheService.getActivitiesCache(pageNumber, version);

            if (activitiesCache.isLater()) {
                log.info("listActivities tryLater: {} | {}", userId, saleActivitiesQuery);
                return AppMultiResult.tryLater();
            }

            activities = activitiesCache.getSaleActivities();
            total = activitiesCache.getTotal();
        } else {
            PageQuery pageQuery = SaleActivityAppConverter.toPageQuery(saleActivitiesQuery);

            // Get activities from domain service
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);

            activities = activitiesPageResult.getData();
            total = activitiesPageResult.getTotal();
        }

        List<SaleActivityDTO> saleActivityDTOS = activities
                .stream()
                .map(SaleActivityAppConverter::toDTO)
                .collect(Collectors.toList());

        log.info("listActivities ok: {} | {}", userId, saleActivitiesQuery);
        return AppMultiResult.of(saleActivityDTOS, total);
    }

    @Override
    public AppResult publishActivity(Long userId, PublishActivityCommand publishActivityCommand) {
        log.info("publishActivity: {} | {}", userId, publishActivityCommand);
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user's permission
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get Redisson distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildCreateKey(userId));

        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Publish activity
            SaleActivity saleActivity = SaleActivityAppConverter.toDomainModel(publishActivityCommand);
            saleActivityDomainService.publishActivity(userId, saleActivity);

            log.info("publishActivity ok: {} | {}", userId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("publishActivity error", e);
            throw new BizException(AppErrorCode.ACTIVITY_PUBLISH_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult modifyActivity(Long userId, Long activityId, PublishActivityCommand publishActivityCommand) {
        log.info("modifyActivity: {} | {} | {}", userId, activityId, publishActivityCommand);
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user's permission
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get Redisson distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));

        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Modify activity
            SaleActivity saleActivity = SaleActivityAppConverter.toDomainModel(publishActivityCommand);
            saleActivity.setId(activityId);
            saleActivityDomainService.modifyActivity(userId, saleActivity);

            log.info("modifyActivity ok: {} | {} | {}", userId, activityId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("modifyActivity error: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult onlineActivity(Long userId, Long activityId) {
        log.info("onlineActivity: {} | {}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Online activity
            saleActivityDomainService.onlineActivity(userId, activityId);

            log.info("onlineActivity ok: {} | {}", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult offlineActivity(Long userId, Long activityId) {
        log.info("offlineActivity: {} | {}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Offline activity
            saleActivityDomainService.offlineActivity(userId, activityId);

            log.info("offlineActivity ok: {} | {}", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public boolean isPlaceOrderAllowed(Long activityId) {
        SaleActivityCache activityCache = saleActivityCacheService.getActivityCache(activityId, null);
        if (activityCache.isLater()) {
            log.info("isPlaceOrderAllowed tryLater: {}", activityId);
            return false;
        }
        if (!activityCache.isExist() || activityCache.getSaleActivity() == null) {
            log.info("isPlaceOrderAllowed activity not found: {}", activityId);
            return false;
        }

        SaleActivity saleActivity = activityCache.getSaleActivity();
        if (!saleActivity.isOnline()) {
            log.info("isPlaceOrderAllowed activity not online: {}", activityId);
            return false;
        }
        if (!saleActivity.isInProgress()) {
            log.info("isPlaceOrderAllowed activity not in progress: {}", activityId);
            return false;
        }

        return true;
    }

    private String buildCreateKey(Long userId) {
        return KeyUtil.link(ACTIVITY_CREATE_LOCK, userId);
    }

    private String buildModificationKey(Long activityId) {
        return KeyUtil.link(ACTIVITY_MODIFICATION_LOCK, activityId);
    }
}
