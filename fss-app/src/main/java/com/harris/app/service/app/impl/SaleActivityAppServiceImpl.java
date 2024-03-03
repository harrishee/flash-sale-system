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
    // 分布式锁的 key 的前缀
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
        log.info("应用层 getActivity: [{},{},{}]", userId, activityId, version);
        if (userId == null || activityId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 从缓存中检索活动缓存对象并检查是否存在
        SaleActivityCache activityCache = saleActivityCacheService.getActivityCache(activityId, version);
        if (!activityCache.isExist()) {
            log.info("应用层 getActivity，活动不存在: [{},{},{}]", userId, activityId, version);
            throw new BizException(AppErrorCode.ACTIVITY_NOT_FOUND.getErrDesc());
        }
        
        // 如果活动缓存对象标志为稍后重试
        if (activityCache.isLater()) {
            log.info("应用层 getActivity，请稍后重试: [{},{},{}]", userId, activityId, version);
            return AppSingleResult.tryLater();
        }
        
        // 从活动缓存对象中获取活动对象和版本号，然后转换为DTO对象
        SaleActivity saleActivity = activityCache.getSaleActivity();
        SaleActivityDTO saleActivityDTO = AppConverter.toDTO(saleActivity);
        saleActivityDTO.setVersion(activityCache.getVersion());
        
        log.info("应用层 getActivity，成功: [{},{},{}]", userId, activityId, version);
        return AppSingleResult.ok(saleActivityDTO);
    }
    
    @Override
    public AppMultiResult<SaleActivityDTO> listActivities(Long userId, SaleActivitiesQuery saleActivitiesQuery) {
        log.info("应用层 listActivities: [{},{}]", userId, saleActivitiesQuery);
        if (userId == null || saleActivitiesQuery == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 声明活动列表和总数变量
        List<SaleActivity> activities;
        Integer total;
        
        // 如果是 第一页 且 不在线状态 的活动查询，从缓存中获取
        // 第一页且不在线状态的活动查询，不需要实时数据，可以从缓存中获取
        if (saleActivitiesQuery.isFirstPageQuery() &&
                !Objects.equals(SaleActivityStatus.ONLINE.getCode(), saleActivitiesQuery.getStatus())) {
            
            // 获取页码和版本号
            Integer pageNumber = saleActivitiesQuery.getPageNumber();
            Long version = saleActivitiesQuery.getVersion();
            
            // 从缓存中获取活动列表缓存对象
            SaleActivitiesCache activitiesCache = saleActivitiesCacheService.getActivitiesCache(pageNumber, version);
            
            // 如果活动列表缓存对象标志为稍后重试
            if (activitiesCache.isLater()) {
                log.info("应用层 listActivities，请稍后重试: [{},{}]", userId, saleActivitiesQuery);
                return AppMultiResult.tryLater();
            }
            
            // 获取活动列表缓存对象中的活动列表和总数
            activities = activitiesCache.getSaleActivities();
            total = activitiesCache.getTotal();
        } else {
            // 将抢购品活动查询参数转换为领域层的分页查询对象，然后调用领域层的服务方法，获取抢购品活动分页结果
            PageQuery pageQuery = AppConverter.toPageQuery(saleActivitiesQuery);
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);
            
            // 获取分页结果中的活动列表和总数
            activities = activitiesPageResult.getData();
            total = activitiesPageResult.getTotal();
        }
        
        // 将活动列表转换为DTO对象列表
        List<SaleActivityDTO> saleActivityDTOS = activities.stream().map(AppConverter::toDTO).collect(Collectors.toList());
        
        log.info("应用层 listActivities，成功: [{},{}]", userId, saleActivitiesQuery);
        return AppMultiResult.of(saleActivityDTOS, total);
    }
    
    @Override
    public AppResult publishActivity(Long userId, PublishActivityCommand publishActivityCommand) {
        log.info("应用层 publishActivity: [{},{}]", userId, publishActivityCommand);
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 进行用户权限认证，确保用户具有发布活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildCreateKey(userId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 转换成领域层的活动对象，然后调用领域层的 发布活动 方法
            SaleActivity saleActivity = AppConverter.toDomainModel(publishActivityCommand);
            saleActivityDomainService.publishActivity(userId, saleActivity);
            
            log.info("应用层 publishActivity，成功: [{},{}]", userId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 publishActivity，异常: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_PUBLISH_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public AppResult modifyActivity(Long userId, Long activityId, PublishActivityCommand publishActivityCommand) {
        log.info("应用层 modifyActivity: [{},{},{}]", userId, activityId, publishActivityCommand);
        if (userId == null || publishActivityCommand == null || publishActivityCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 进行用户权限认证，确保用户具有修改活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 转换成领域层的活动对象并设置活动ID，然后调用领域层的 修改活动 方法
            SaleActivity saleActivity = AppConverter.toDomainModel(publishActivityCommand);
            saleActivity.setId(activityId);
            saleActivityDomainService.modifyActivity(userId, saleActivity);
            
            log.info("应用层 modifyActivity，成功: [{},{},{}]", userId, activityId, publishActivityCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 modifyActivity，异常: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public AppResult onlineActivity(Long userId, Long activityId) {
        log.info("应用层 onlineActivity: [{},{}]", userId, activityId);
        if (userId == null || activityId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 进行用户权限认证，确保用户具有上线活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_CREATE);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 上线活动 方法
            saleActivityDomainService.onlineActivity(userId, activityId);
            
            log.info("应用层 onlineActivity，成功: [{},{}]", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 onlineActivity，异常: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public AppResult offlineActivity(Long userId, Long activityId) {
        log.info("应用层 offlineActivity: [{},{}]", userId, activityId);
        if (userId == null || activityId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 进行用户权限认证，确保用户具有下线活动的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ACTIVITY_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationKey(activityId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 下线活动 方法
            saleActivityDomainService.offlineActivity(userId, activityId);
            
            log.info("应用层 offlineActivity，成功: [{},{}]", userId, activityId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 offlineActivity，异常: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public boolean isPlaceOrderAllowed(Long activityId) {
        // 从缓存中获取活动缓存对象
        SaleActivityCache activityCache = saleActivityCacheService.getActivityCache(activityId, null);
        
        // 如果活动缓存对象标志为稍后重试
        if (activityCache.isLater()) {
            log.info("应用层 isPlaceOrderAllowed，请稍后重试: [{}]", activityId);
            return false;
        }
        
        // 如果活动缓存对象标志为不存在或者其活动对象为空
        if (!activityCache.isExist() || activityCache.getSaleActivity() == null) {
            log.info("应用层 isPlaceOrderAllowed，活动不存在: [{}]", activityId);
            return false;
        }
        
        // 获取活动缓存对象中的活动对象
        SaleActivity saleActivity = activityCache.getSaleActivity();
        
        // 如果活动不在线
        if (!saleActivity.isOnline()) {
            log.info("应用层 isPlaceOrderAllowed，活动不在线: [{}]", activityId);
            return false;
        }
        
        // 如果活动不在进行中
        if (!saleActivity.isInProgress()) {
            log.info("应用层 isPlaceOrderAllowed，活动不在进行中: [{}]", activityId);
            return false;
        }
        
        // 如果以上条件都满足，则表示可以下单，返回 true
        return true;
    }
    
    // 构建用于创建活动的分布式锁的 key
    private String buildCreateKey(Long userId) {
        return KeyUtil.link(ACTIVITY_CREATE_LOCK, userId);
    }
    
    // 构建用于修改活动的分布式锁的 key
    private String buildModificationKey(Long activityId) {
        return KeyUtil.link(ACTIVITY_MODIFICATION_LOCK, activityId);
    }
}
