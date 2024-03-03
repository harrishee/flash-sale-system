package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.ResourceEnum;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.cache.SaleItemCache;
import com.harris.app.model.cache.SaleItemsCache;
import com.harris.app.model.cache.StockCache;
import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.util.AppConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.AuthService;
import com.harris.app.service.app.SaleItemAppService;
import com.harris.app.service.cache.SaleItemCacheService;
import com.harris.app.service.cache.SaleItemsCacheService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.SaleActivityDomainService;
import com.harris.domain.service.SaleItemDomainService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import com.harris.infra.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaleItemAppServiceImpl implements SaleItemAppService {
    // 分布式锁的 key 的前缀
    private static final String ITEM_CREATE_LOCK_KEY = "ITEM_CREATE_LOCK_KEY";
    private static final String ITEM_MODIFICATION_LOCK_KEY = "ITEM_MODIFICATION_LOCK_KEY";
    
    @Resource
    private AuthService authService;
    
    @Resource
    private SaleItemDomainService saleItemDomainService;
    
    @Resource
    private SaleActivityDomainService saleActivityDomainService;
    
    @Resource
    private SaleItemCacheService saleItemCacheService;
    
    @Resource
    private SaleItemsCacheService saleItemsCacheService;
    
    @Resource
    private StockCacheService stockCacheService;
    
    @Resource
    private DistributedLockService distributedLockService;
    
    @Override
    public AppSingleResult<SaleItemDTO> getItem(Long itemId) {
        log.info("应用层 getItem: [{}]", itemId);
        if (itemId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 从缓存中获取抢购品缓存对象
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, null);
        if (itemCache.isLater()) {
            log.info("应用层 getItem，请稍后重试: [{}]", itemId);
            return AppSingleResult.tryLater();
        }
        
        // 如果抢购品缓存对象不存在，或者抢购品缓存对象中的抢购品为空
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            log.info("应用层 getItem，抢购品不存在: [{}]", itemId);
            throw new BizException(AppErrorCode.ITEM_NOT_FOUND.getErrDesc());
        }
        
        // 更新最新的抢购品库存
        updateLatestItemStock(null, itemCache.getSaleItem());
        
        // 将抢购品缓存对象中的抢购品信息转换为 DTO 对象，并设置版本号
        SaleItemDTO saleItemDTO = AppConverter.toDTO(itemCache.getSaleItem());
        saleItemDTO.setVersion(itemCache.getVersion());
        
        log.info("应用层 getItem，成功: [{}]", itemId);
        return AppSingleResult.ok(saleItemDTO);
    }
    
    @Override
    public AppSingleResult<SaleItemDTO> getItem(Long userId, Long activityId, Long itemId, Long version) {
        log.info("应用层 getItem: [{},{},{},{}]", userId, activityId, itemId, version);
        if (userId == null || activityId == null || itemId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 从缓存中获取抢购品缓存对象
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, version);
        if (itemCache.isLater()) {
            log.info("应用层 getItem，请稍后重试: [{},{},{}]", userId, activityId, itemId);
            return AppSingleResult.tryLater();
        }
        
        // 如果抢购品缓存对象不存在，或者抢购品缓存对象中的抢购品为空
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            log.info("应用层 getItem，抢购品不存在: [{},{},{}]", userId, activityId, itemId);
            throw new BizException(AppErrorCode.ITEM_NOT_FOUND.getErrDesc());
        }
        
        // 更新最新的抢购品库存
        updateLatestItemStock(userId, itemCache.getSaleItem());
        
        // 将抢购品缓存对象中的抢购品信息转换为 DTO 对象，并设置版本号
        SaleItemDTO saleItemDTO = AppConverter.toDTO(itemCache.getSaleItem());
        saleItemDTO.setVersion(itemCache.getVersion());
        
        log.info("应用层 getItem，成功: [{},{},{}]", userId, activityId, itemId);
        return AppSingleResult.ok(saleItemDTO);
    }
    
    private void updateLatestItemStock(Long userId, SaleItem saleItem) {
        if (saleItem == null) return;
        
        // 从缓存中获取库存缓存对象，并把可用库存设置到抢购品对象中
        StockCache stockCache = stockCacheService.getStockCache(userId, saleItem.getId());
        if (stockCache != null && stockCache.isSuccess() && stockCache.getAvailableStockQuantity() != null) {
            saleItem.setAvailableStock(stockCache.getAvailableStockQuantity());
        }
    }
    
    @Override
    public AppMultiResult<SaleItemDTO> listItems(Long userId, Long activityId, SaleItemsQuery saleItemsQuery) {
        log.info("应用层 listItems: [{},{},{}]", userId, activityId, saleItemsQuery);
        if (saleItemsQuery == null) return AppMultiResult.empty();
        
        // 设置查询条件中的活动ID
        saleItemsQuery.setActivityId(activityId);
        
        // 声明抢购品列表和总数变量
        List<SaleItem> items;
        Integer total;
        
        // 如果是在线上第一页的查询，则从缓存中获取值
        // 第一页 且 在线 的抢购品列表属于热点数据，需要从缓存中获取？TODO
        if (saleItemsQuery.isOnlineFirstPageQuery()) {
            // 从缓存中获取抢购品列表缓存对象
            SaleItemsCache itemsCache = saleItemsCacheService.getItemsCache(activityId, saleItemsQuery.getVersion());
            if (itemsCache.isLater()) {
                log.info("应用层 listItems，请稍后重试: [{},{},{}]", userId, activityId, saleItemsQuery);
                return AppMultiResult.tryLater();
            }
            
            // 如果抢购品列表缓存对象为空，则返回空结果
            if (itemsCache.isEmpty()) {
                log.info("应用层 listItems，无抢购品: [{},{},{}]", userId, activityId, saleItemsQuery);
                return AppMultiResult.empty();
            }
            
            // 从抢购品列表缓存对象中获取抢购品列表和总数
            items = itemsCache.getSaleItems();
            total = itemsCache.getTotal();
        } else {
            // 否则，从领域服务中获取抢购品信息
            PageQuery condition = AppConverter.toPageQuery(saleItemsQuery);
            PageResult<SaleItem> itemsPageResult = saleItemDomainService.getItems(condition);
            
            // 获取领域服务返回的抢购品列表和总数
            items = itemsPageResult.getData();
            total = itemsPageResult.getTotal();
        }
        
        // 如果抢购品列表为空，则返回空结果
        if (CollectionUtils.isEmpty(items)) {
            log.info("应用层 listItems，无抢购品: [{},{},{}]", userId, activityId, saleItemsQuery);
            return AppMultiResult.empty();
        }
        
        // 将抢购品列表转换为 DTO 对象列表
        List<SaleItemDTO> saleItemDTOS = items.stream().map(AppConverter::toDTO).collect(Collectors.toList());
        
        log.info("应用层 listItems，成功: [{},{},{}]", userId, activityId, saleItemsQuery);
        return AppMultiResult.of(saleItemDTOS, total);
    }
    
    @Override
    public AppResult publishItem(Long userId, Long activityId, PublishItemCommand publishItemCommand) {
        log.info("应用层 publishItem: [{},{},{}]", userId, activityId, publishItemCommand);
        if (userId == null || activityId == null || publishItemCommand == null || publishItemCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 进行用户权限认证，确保用户具有发布抢购品的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ITEM_CREATE);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildCreateLockKey(userId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域服务获取活动信息，如果活动不存在则抛出异常
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            if (saleActivity == null) throw new BizException(AppErrorCode.ACTIVITY_NOT_FOUND);
            
            // 转换成领域层的抢购品对象，并设置活动ID和库存预热值，然后调用领域层的 发布抢购品 方法
            SaleItem saleItem = AppConverter.toDomainModel(publishItemCommand);
            saleItem.setActivityId(activityId);
            saleItem.setStockWarmUp(0);
            saleItemDomainService.publishItem(saleItem);
            
            log.info("应用层 publishItem，成功: [{},{},{}]", userId, activityId, publishItemCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 publishItem，异常: ", e);
            throw new BizException(AppErrorCode.ITEM_PUBLISH_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public AppResult onlineItem(Long userId, Long activityId, Long itemId) {
        log.info("应用层 onlineItem: [{},{},{}]", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 进行用户权限认证，确保用户具有修改抢购品的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationLockKey(userId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域服务的 上线抢购品 方法
            saleItemDomainService.onlineItem(itemId);
            
            log.info("应用层 onlineItem，成功: [{},{},{}]", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 onlineItem，异常: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public AppResult offlineItem(Long userId, Long activityId, Long itemId) {
        log.info("应用层 offlineItem: [{},{},{}]", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) throw new BizException(AppErrorCode.INVALID_PARAMS);
        
        // 进行用户权限认证，确保用户具有修改抢购品的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationLockKey(userId));
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域服务的 下线抢购品 方法
            saleItemDomainService.offlineItem(itemId);
            
            log.info("应用层 offlineItem，成功: [{},{},{}]", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 offlineItem，异常: ", e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }
    
    @Override
    public boolean isPlaceOrderAllowed(Long itemId) {
        // 从缓存中获取抢购品信息，并检查是否允许下单
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, null);
        
        // 如果抢购品信息尚未缓存完毕
        if (itemCache.isLater()) {
            log.info("应用层 isPlaceOrderAllowed，请稍后重试: [{}]", itemId);
            return false;
        }
        
        // 如果抢购品信息不存在，或者抢购品信息中的抢购品为空
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            log.info("应用层 isPlaceOrderAllowed，抢购品不存在: [{}]", itemId);
            return false;
        }
        
        // 如果抢购品信息中的抢购品不是上线状态，或者抢购品信息中的抢购品不是进行中状态
        if (!itemCache.getSaleItem().isOnline()) {
            log.info("应用层 isPlaceOrderAllowed，抢购品未上线: [{}]", itemId);
            return false;
        }
        
        // 如果抢购品信息中的抢购品不是进行中状态
        if (!itemCache.getSaleItem().isInProgress()) {
            log.info("应用层 isPlaceOrderAllowed，抢购品未开始: [{}]", itemId);
            return false;
        }
        
        // 如果以上条件都满足，则表示可以下单，返回 true
        return true;
    }
    
    // 构建用于创建抢购品的分布式锁的 key
    private String buildCreateLockKey(Long userId) {
        return KeyUtil.link(ITEM_CREATE_LOCK_KEY, userId);
    }
    
    // 构建用于修改抢购品的分布式锁的 key
    private String buildModificationLockKey(Long itemId) {
        return KeyUtil.link(ITEM_MODIFICATION_LOCK_KEY, itemId);
    }
}
