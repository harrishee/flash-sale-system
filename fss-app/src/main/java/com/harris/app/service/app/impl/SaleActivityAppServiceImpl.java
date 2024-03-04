package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.ResourceEnum;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.util.AppConverter;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.AuthService;
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
    private AuthService authService;
    
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
        if (userId == null || activityId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        log.info("应用层 getActivity: [userId={}, activityId={}, version={}]", userId, activityId, version);
        
        // 从缓存中获取 活动缓存对象
        SaleActivityCache activityCache = saleActivityCacheService.getActivityCache(activityId, version);
        if (!activityCache.isExist() || activityCache.getSaleActivity() == null) {
            throw new BizException(AppErrorCode.ACTIVITY_NOT_FOUND.getErrDesc());
        }
        if (activityCache.isLater()) {
            log.info("应用层 getActivity，请稍后重试: [userId={}, activityId={}, version={}]", userId, activityId, version);
            return AppSingleResult.tryLater();
        }
        
        // 从 活动缓存对象 中获取 活动对象 和 版本号
        SaleActivity saleActivity = activityCache.getSaleActivity();
        SaleActivityDTO saleActivityDTO = AppConverter.toDTO(saleActivity);
        saleActivityDTO.setVersion(activityCache.getVersion());
        
        log.info("应用层 getActivity，成功: [userId={}, activityId={}, version={}]", userId, activityId, version);
        return AppSingleResult.ok(saleActivityDTO);
    }
    
    @Override
    public AppMultiResult<SaleActivityDTO> listActivities(Long userId, SaleActivitiesQuery saleActivitiesQuery) {
        if (userId == null || saleActivitiesQuery == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        log.info("应用层 listActivities: [userId={}, saleActivitiesQuery={}]", userId, saleActivitiesQuery);
        
        List<SaleActivity> activities;
        Integer total;
        
        // 查询 第一页 且 不为ONLINE 的活动列表，走缓存，因为数据变更不频繁
        if (saleActivitiesQuery.isFirstPageQuery() && !Objects.equals(SaleActivityStatus.ONLINE.getCode(), saleActivitiesQuery.getStatus())) {
            log.info("应用层 listActivities，走缓存");
            // 从缓存中获取 活动列表缓存对象
            Integer pageNumber = saleActivitiesQuery.getPageNumber();
            Long version = saleActivitiesQuery.getVersion();
            SaleActivitiesCache activitiesCache = saleActivitiesCacheService.getActivitiesCache(pageNumber, version);
            if (activitiesCache.isLater()) return AppMultiResult.tryLater();
            
            // 获取缓存的结果
            activities = activitiesCache.getSaleActivities();
            total = activitiesCache.getTotal();
        } else {
            log.info("应用层 listActivities，走数据库");
            // 从领域层获取 活动列表
            PageQuery pageQuery = AppConverter.toPageQuery(saleActivitiesQuery);
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);
            
            // 获取数据库的结果
            activities = activitiesPageResult.getData();
            total = activitiesPageResult.getTotal();
        }
        
        List<SaleActivityDTO> saleActivityDTOS = activities.stream().map(AppConverter::toDTO).collect(Collectors.toList());
        log.info("应用层 listActivities，成功: [uerId={}, total={}]", userId, total);
        return AppMultiResult.of(saleActivityDTOS, total);
    }
    
    @Override
    public AppResult publishActivity(Long userId, PublishActivityCommand publishActivityCommand) {
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        log.info("应用层 publishActivity: [userId={}, publishActivityCommand={}]", userId, publishActivityCommand);
        
        // 进行用户权限认证，确保用户具有发布活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁实例，key = ACTIVITY_CREATE_LOCK + userId
        DistributedLock rLock = distributedLockService.getLock(buildCreateKey(userId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 发布活动 方法
            SaleActivity saleActivity = AppConverter.toDomainModel(publishActivityCommand);
            saleActivityDomainService.publishActivity(userId, saleActivity);
            
            log.info("应用层 publishActivity，成功: [{},{}]", userId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 publishActivity，异常: [userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ACTIVITY_PUBLISH_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public AppResult modifyActivity(Long userId, Long activityId, PublishActivityCommand publishActivityCommand) {
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        log.info("应用层 modifyActivity: [userId={}, activityId={}, publishActivityCommand={}]", userId, activityId, publishActivityCommand);
        
        // 进行用户权限认证，确保用户具有修改活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁实例，key = ACTIVITY_MODIFICATION_LOCK + activityId
        DistributedLock rLock = distributedLockService.getLock(buildModificationKey(activityId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 修改活动 方法
            SaleActivity saleActivity = AppConverter.toDomainModel(publishActivityCommand);
            saleActivity.setId(activityId);
            saleActivityDomainService.modifyActivity(userId, saleActivity);
            
            log.info("应用层 modifyActivity，成功: [userId={}, activityId={}, publishActivityCommand={}]", userId, activityId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 modifyActivity，异常: [userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public AppResult onlineActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        log.info("应用层 onlineActivity: [userId={}, activityId={}]", userId, activityId);
        
        // 进行用户权限认证，确保用户具有上线活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁实例，key = ACTIVITY_MODIFICATION_LOCK + activityId
        DistributedLock rLock = distributedLockService.getLock(buildModificationKey(activityId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 上线活动 方法
            saleActivityDomainService.onlineActivity(userId, activityId);
            
            log.info("应用层 onlineActivity，成功: [userId={}, activityId={}]", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 onlineActivity，异常:[userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public AppResult offlineActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        log.info("应用层 offlineActivity: [userId={}, activityId={}]", userId, activityId);
        
        // 进行用户权限认证，确保用户具有下线活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁实例，key = ACTIVITY_MODIFICATION_LOCK + activityId
        DistributedLock rLock = distributedLockService.getLock(buildModificationKey(activityId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 下线活动 方法
            saleActivityDomainService.offlineActivity(userId, activityId);
            
            log.info("应用层 offlineActivity，成功: [userId={}, activityId={}]", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 offlineActivity，异常: [userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public boolean isPlaceOrderAllowed(Long activityId) {
        // 从缓存中获取 活动缓存对象
        SaleActivityCache activityCache = saleActivityCacheService.getActivityCache(activityId, null);
        if (activityCache.isLater()) {
            log.info("应用层 isPlaceOrderAllowed，请稍后重试: [{}]", activityId);
            return false;
        }
        if (!activityCache.isExist() || activityCache.getSaleActivity() == null) {
            log.info("应用层 isPlaceOrderAllowed，活动不存在: [{}]", activityId);
            return false;
        }
        if (!activityCache.getSaleActivity().isOnline()) {
            log.info("应用层 isPlaceOrderAllowed，活动不在线: [{}]", activityId);
            return false;
        }
        if (!activityCache.getSaleActivity().isInProgress()) {
            log.info("应用层 isPlaceOrderAllowed，活动不在进行中: [{}]", activityId);
            return false;
        }
        
        // 以上条件都满足，可以下单
        return true;
    }
    
    private String buildCreateKey(Long userId) {
        return KeyUtil.link(ACTIVITY_CREATE_LOCK, userId);
    }
    
    private String buildModificationKey(Long activityId) {
        return KeyUtil.link(ACTIVITY_MODIFICATION_LOCK, activityId);
    }
}
