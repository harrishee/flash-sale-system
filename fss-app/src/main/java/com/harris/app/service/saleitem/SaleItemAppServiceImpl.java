package com.harris.app.service.saleitem;

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
import com.harris.app.service.auth.AuthService;
import com.harris.app.service.stock.StockCacheService;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.activity.SaleActivityDomainService;
import com.harris.domain.service.item.SaleItemDomainService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.distributed.lock.DistributedLock;
import com.harris.infra.distributed.lock.DistributedLockService;
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
    public AppSingleResult<SaleItemDTO> getItem(Long userId, Long activityId, Long itemId, Long version) {
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 从缓存中获取商品缓存对象
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, version);
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            throw new BizException(AppErrorCode.ITEM_NOT_FOUND.getErrDesc());
        }
        if (itemCache.isLater()) {
            log.info("应用层 getItem，请稍后重试: [userId={}, activityId={}, itemId={}, version={}]", userId, activityId, itemId, version);
            return AppSingleResult.tryLater();
        }
        
        // 再次更新最新的 商品库存
        updateLatestStockFromStockCache(userId, itemCache.getSaleItem());
        
        // 从商品缓存对象中获取 商品对象 和 版本号
        SaleItemDTO saleItemDTO = AppConverter.toDTO(itemCache.getSaleItem());
        saleItemDTO.setVersion(itemCache.getVersion());
        
        return AppSingleResult.ok(saleItemDTO);
    }
    
    @Override
    public AppSingleResult<SaleItemDTO> getItem(Long itemId) {
        if (itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 从缓存中获取商品缓存对象
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, null);
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            throw new BizException(AppErrorCode.ITEM_NOT_FOUND.getErrDesc());
        }
        if (itemCache.isLater()) {
            log.info("应用层 getItem，请稍后重试: [{}]", itemId);
            return AppSingleResult.tryLater();
        }
        
        // 再次更新最新的 商品库存
        updateLatestStockFromStockCache(null, itemCache.getSaleItem());
        
        // 从商品缓存对象中获取 商品对象 和 版本号
        SaleItemDTO saleItemDTO = AppConverter.toDTO(itemCache.getSaleItem());
        saleItemDTO.setVersion(itemCache.getVersion());
        
        return AppSingleResult.ok(saleItemDTO);
    }
    
    private void updateLatestStockFromStockCache(Long userId, SaleItem saleItem) {
        if (saleItem == null) return;
        
        // 从缓存中获取 库存缓存对象
        StockCache stockCache = stockCacheService.getStockCache(userId, saleItem.getId());
        if (stockCache != null && stockCache.isSuccess() && stockCache.getAvailableStock() != null) {
            // 更新 商品对象 的 可用库存
            saleItem.setAvailableStock(stockCache.getAvailableStock());
        }
    }
    
    @Override
    public AppMultiResult<SaleItemDTO> listItems(Long userId, Long activityId, SaleItemsQuery saleItemsQuery) {
        if (saleItemsQuery == null) {
            return AppMultiResult.empty();
        }
        
        // 绑定活动ID
        saleItemsQuery.setActivityId(activityId);
        List<SaleItem> items;
        Integer total;
        
        // 第一页 且 没有关键字 且 在线 的查询，走缓存
        if (saleItemsQuery.isOnlineFirstPageAndNoKeywordQuery()) {
            // 从缓存中获取 商品列表缓存
            SaleItemsCache itemsCache = saleItemsCacheService.getItemsCache(activityId, saleItemsQuery.getVersion());
            if (itemsCache.isLater()) return AppMultiResult.tryLater();
            
            // 获取缓存的结果
            items = itemsCache.getSaleItems();
            total = itemsCache.getTotal();
        } else {
            // 从领域层获取 商品列表
            PageQuery condition = AppConverter.toPageQuery(saleItemsQuery);
            PageResult<SaleItem> itemsPageResult = saleItemDomainService.getItems(condition);
            
            // 获取数据库的结果
            items = itemsPageResult.getData();
            total = itemsPageResult.getTotal();
        }
        
        if (CollectionUtils.isEmpty(items)) {
            return AppMultiResult.empty();
        }
        
        List<SaleItemDTO> saleItemDTOS = items.stream().map(AppConverter::toDTO).collect(Collectors.toList());
        return AppMultiResult.of(saleItemDTOS, total);
    }
    
    @Override
    public AppResult publishItem(Long userId, Long activityId, PublishItemCommand publishItemCommand) {
        if (userId == null || activityId == null || publishItemCommand == null || publishItemCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 进行用户权限认证，确保用户具有发布商品的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ITEM_CREATE);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 获取分布锁实例，用户防抖，key = ITEM_CREATE_LOCK_KEY + userId
        DistributedLock rLock = distributedLockService.getLock(buildCreateLockKey(userId));
        try {
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 检查活动是否存在
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            if (saleActivity == null) throw new BizException(AppErrorCode.ACTIVITY_NOT_FOUND);
            
            // 调用领域层的 发布商品 方法
            SaleItem saleItem = AppConverter.toDomainModel(publishItemCommand);
            saleItem.setActivityId(activityId);
            saleItem.setStockWarmUp(0);
            saleItemDomainService.publishItem(saleItem);
            
            log.info("应用层 publishItem，成功: [userId={}, activityId={}, publishItemCommand={}]", userId, activityId, publishItemCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 publishItem，异常: [userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ITEM_PUBLISH_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public AppResult onlineItem(Long userId, Long activityId, Long itemId) {
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 进行用户权限认证，确保用户具有修改抢购品的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 应用层加分布式锁，防止并发修改商品，key = ITEM_MODIFICATION_LOCK_KEY + userId
        DistributedLock rLock = distributedLockService.getLock(buildModificationLockKey(userId));
        try {
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 修改商品 方法
            saleItemDomainService.onlineItem(itemId);
            
            log.info("应用层 onlineItem，成功: [userId={}, activityId={}, itemId={}]", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 onlineItem，异常:[userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public AppResult offlineItem(Long userId, Long activityId, Long itemId) {
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }
        
        // 进行用户权限认证，确保用户具有修改抢购品的权限
        AuthResult authResult = authService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        
        // 应用层加分布式锁，防止并发修改商品，key = ITEM_MODIFICATION_LOCK_KEY + userId
        DistributedLock rLock = distributedLockService.getLock(buildModificationLockKey(userId));
        try {
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) throw new BizException(AppErrorCode.LOCK_FAILED);
            
            // 调用领域层的 下线商品 方法
            saleItemDomainService.offlineItem(itemId);
            
            log.info("应用层 offlineItem，成功: [userId={}, activityId={}, itemId={}]", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("应用层 offlineItem，异常: [userId={}] ", userId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public boolean isPlaceOrderAllowed(Long itemId) {
        // 从缓存中获取 商品缓存
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, null);
        if (itemCache.isLater()) {
            log.info("应用层商品检查，请稍后重试: [itemId={}]", itemId);
            return false;
        }
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            log.info("应用层商品检查，商品不存在: [itemId={}]", itemId);
            return false;
        }
        if (!itemCache.getSaleItem().isOnline()) {
            log.info("应用层商品检查，商品未上线: [itemId={}]", itemId);
            return false;
        }
        if (!itemCache.getSaleItem().isInProgress()) {
            log.info("应用层商品检查，商品未开始: [itemId={}]", itemId);
            return false;
        }
        
        // 以上条件都满足，可以下单
        return true;
    }
    
    private String buildCreateLockKey(Long userId) {
        return KeyUtil.link(ITEM_CREATE_LOCK_KEY, userId);
    }
    
    private String buildModificationLockKey(Long itemId) {
        return KeyUtil.link(ITEM_MODIFICATION_LOCK_KEY, itemId);
    }
}
