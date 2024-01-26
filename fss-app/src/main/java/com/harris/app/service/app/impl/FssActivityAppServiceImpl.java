package com.harris.app.service.app.impl;

import com.harris.app.auth.AuthAppService;
import com.harris.app.auth.model.AuthResult;
import com.harris.app.auth.model.ResourceEnum;
import com.harris.app.exception.AppErrCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.converter.SaleActivityAppConverter;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.FssActivityAppService;
import com.harris.app.service.cache.FssActivitiesCacheService;
import com.harris.app.service.cache.FssActivityCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.service.FssActivityDomainService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssActivityAppServiceImpl implements FssActivityAppService {
    public static final String ACTIVITY_CREATE_LOCK = "ACTIVITY_CREATE_LOCK";
    public static final String ACTIVITY_MODIFICATION_LOCK = "ACTIVITY_MODIFICATION_LOCK";

    @Resource
    private AuthAppService authAppService;

    @Resource
    private FssActivityDomainService fssActivityDomainService;

    @Resource
    private FssActivityCacheService fssActivityCacheService;

    @Resource
    private FssActivitiesCacheService fssActivitiesCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    public AppSingleResult<SaleActivityDTO> getActivity(Long userId, Long activityId, Long version) {
        log.info("App getActivity: {},{},{}", userId, activityId, version);
        if (userId == null || activityId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Retrieve activity from cache and check if it exists
        SaleActivityCache saleActivityCache = fssActivityCacheService.getActivityCache(activityId, version);
        if (!saleActivityCache.isExist()) {
            throw new BizException(AppErrCode.ACTIVITY_NOT_FOUND.getErrDesc());
        }

        // Return tryLater if the cache signal is later
        if (saleActivityCache.isLater()) {
            log.info("App getActivity tryLater: {},{},{}", userId, activityId, version);
            return AppSingleResult.tryLater();
        }

        // Get SaleActivity object and convert it to DTO with version
        SaleActivity saleActivity = saleActivityCache.getSaleActivity();
        SaleActivityDTO saleActivityDTO = SaleActivityAppConverter.toDTO(saleActivity);
        saleActivityDTO.setVersion(saleActivityCache.getVersion());

        log.info("App getActivity ok: {},{},{}", userId, activityId, version);
        return AppSingleResult.ok(saleActivityDTO);
    }

    @Override
    public AppMultiResult<SaleActivityDTO> listActivities(Long userId, SaleActivitiesQuery saleActivitiesQuery) {
        log.info("App listActivities: {},{}", userId, saleActivitiesQuery);
        if (saleActivitiesQuery == null) {
            return AppMultiResult.empty();
        }

        List<SaleActivity> activities;
        Integer total;
        if (saleActivitiesQuery.isFirstPageQuery()) {
            // Set values from cache if it is the first page query
            Integer pageNumber = saleActivitiesQuery.getPageNumber();
            Long version = saleActivitiesQuery.getVersion();
            SaleActivitiesCache saleActivitiesCache = fssActivitiesCacheService.getActivitiesCache(pageNumber, version);
            if (saleActivitiesCache.isLater()) {
                log.info("App listActivities tryLater: {},{}", userId, saleActivitiesQuery);
                return AppMultiResult.tryLater();
            }
            activities = saleActivitiesCache.getSaleActivities();
            total = saleActivitiesCache.getTotal();
        } else {
            // Otherwise, set values from domain service
            PageQueryCondition condition = SaleActivityAppConverter.toCondition(saleActivitiesQuery);
            PageResult<SaleActivity> activitiesPageResult = fssActivityDomainService.getActivities(condition);
            activities = activitiesPageResult.getData();
            total = activitiesPageResult.getTotal();
        }

        // Convert to DTOs
        List<SaleActivityDTO> saleActivityDTOS = activities
                .stream()
                .map(SaleActivityAppConverter::toDTO)
                .collect(Collectors.toList());
        log.info("App listActivities ok: {},{}", userId, saleActivitiesQuery);
        return AppMultiResult.of(saleActivityDTOS, total);
    }

    @Override
    public AppResult publishActivity(Long userId, PublishActivityCommand publishActivityCommand) {
        log.info("App publishActivity: {},{}", userId, publishActivityCommand);
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildCreateKey(userId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLocked = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Publish activity
            SaleActivity saleActivity = SaleActivityAppConverter.toDomainModel(publishActivityCommand);
            fssActivityDomainService.publishActivity(userId, saleActivity);

            log.info("App publishActivity ok: {},{}", userId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App publishActivity failed: {},{}", userId, publishActivityCommand, e);
            throw new BizException(AppErrCode.ACTIVITY_PUBLISH_FAILED);
        } finally {
            // Release lock
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult modifyActivity(Long userId, Long activityId, PublishActivityCommand publishActivityCommand) {
        log.info("App modifyActivity: {},{},{}", userId, activityId, publishActivityCommand);
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLocked = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Modify activity
            SaleActivity saleActivity = SaleActivityAppConverter.toDomainModel(publishActivityCommand);
            saleActivity.setId(activityId);
            fssActivityDomainService.modifyActivity(userId, saleActivity);

            log.info("App modifyActivity ok: {},{},{}", userId, activityId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App modifyActivity failed: {},{},{}", userId, activityId, publishActivityCommand, e);
            throw new BizException(AppErrCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            // Release lock
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult onlineActivity(Long userId, Long activityId) {
        log.info("App onlineActivity: {},{}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLocked = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Online activity
            fssActivityDomainService.onlineActivity(userId, activityId);
            log.info("App onlineActivity ok: {},{}", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException(AppErrCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            // Release lock
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult offlineActivity(Long userId, Long activityId) {
        log.info("App offlineActivity: {},{}", userId, activityId);
        if (userId == null || activityId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLockSuccess) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Offline activity
            fssActivityDomainService.offlineActivity(userId, activityId);
            log.info("App offlineActivity ok: {},{}", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException(AppErrCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            // Release lock
            distributedLock.unlock();
        }
    }

    @Override
    public boolean isPlaceOrderAllowed(Long activityId) {
        SaleActivityCache saleActivityCache = fssActivityCacheService.getActivityCache(activityId, null);
        if (saleActivityCache.isLater()) {
            log.info("App isPlaceOrderAllowed tryLater: {}", activityId);
            return false;
        }
        if (!saleActivityCache.isExist() || saleActivityCache.getSaleActivity() == null) {
            log.info("App isPlaceOrderAllowed activity not found: {}", activityId);
            return false;
        }

        SaleActivity saleActivity = saleActivityCache.getSaleActivity();
        if (!saleActivity.isOnline()) {
            log.info("App isPlaceOrderAllowed activity not online: {}", activityId);
            return false;
        }
        if (!saleActivity.isInProgress()) {
            log.info("App isPlaceOrderAllowed activity not in progress: {}", activityId);
            return false;
        }
        return true;
    }

    private String buildCreateKey(Long userId) {
        return link(ACTIVITY_CREATE_LOCK, userId);
    }

    private String buildModificationKey(Long activityId) {
        return link(ACTIVITY_MODIFICATION_LOCK, activityId);
    }
}
