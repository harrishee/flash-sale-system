package com.harris.app.service.app.impl;

import com.harris.app.auth.AuthAppService;
import com.harris.app.auth.model.AuthResult;
import com.harris.app.auth.model.ResourceEnum;
import com.harris.app.exception.AppErrCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.cache.SaleItemCache;
import com.harris.app.model.cache.SaleItemsCache;
import com.harris.app.model.cache.ItemStockCache;
import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.converter.FssItemAppConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.cache.FssItemCacheService;
import com.harris.app.service.cache.FssItemsCacheService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.app.service.app.FssItemAppService;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.FssActivityDomainService;
import com.harris.domain.service.FssItemDomainService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssItemAppServiceImpl implements FssItemAppService {
    private static final String ITEM_CREATE_LOCK_KEY = "ITEM_CREATE_LOCK_KEY";
    private static final String ITEM_MODIFICATION_LOCK_KEY = "ITEM_MODIFICATION_LOCK_KEY";

    @Resource
    private AuthAppService authAppService;

    @Resource
    private FssItemDomainService fssItemDomainService;

    @Resource
    private FssActivityDomainService fssActivityDomainService;

    @Resource
    private FssItemCacheService fssItemCacheService;

    @Resource
    private FssItemsCacheService fssItemsCacheService;

    @Resource
    private StockCacheService stockCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    public AppSingleResult<SaleItemDTO> getItem(Long itemId) {
        log.info("App getItem: {}", itemId);
        if (itemId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Retrieve item from cache and validate
        SaleItemCache saleItemCache = fssItemCacheService.getItemCache(itemId, null);
        if (saleItemCache.isLater()) {
            log.info("App getItem tryLater: {}", itemId);
            return AppSingleResult.tryLater();
        }
        if (!saleItemCache.isExist() || saleItemCache.getSaleItem() == null) {
            throw new BizException(AppErrCode.ITEM_NOT_FOUND.getErrDesc());
        }

        // Update the latest item stock
        updateLatestItemStock(null, saleItemCache.getSaleItem());
        SaleItemDTO saleItemDTO = FssItemAppConverter.toDTO(saleItemCache.getSaleItem());
        saleItemDTO.setVersion(saleItemCache.getVersion());

        log.info("App getItem ok: {}", itemId);
        return AppSingleResult.ok(saleItemDTO);
    }

    @Override
    public AppSingleResult<SaleItemDTO> getItem(Long userId, Long activityId, Long itemId, Long version) {
        log.info("App getItem: {},{},{}", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Retrieve item from cache and validate
        SaleItemCache saleItemCache = fssItemCacheService.getItemCache(itemId, version);
        if (saleItemCache.isLater()) {
            log.info("App getItem tryLater: {},{},{}", userId, activityId, itemId);
            return AppSingleResult.tryLater();
        }
        if (!saleItemCache.isExist() || saleItemCache.getSaleItem() == null) {
            throw new BizException(AppErrCode.ITEM_NOT_FOUND.getErrDesc());
        }

        // Update the latest item stock
        updateLatestItemStock(userId, saleItemCache.getSaleItem());
        SaleItemDTO saleItemDTO = FssItemAppConverter.toDTO(saleItemCache.getSaleItem());
        saleItemDTO.setVersion(saleItemCache.getVersion());

        log.info("App getItem ok: {},{},{}", userId, activityId, itemId);
        return AppSingleResult.ok(saleItemDTO);
    }

    private void updateLatestItemStock(Long userId, SaleItem saleItem) {
        if (saleItem == null) {
            return;
        }

        // Get the available item stock cache, and update the available stock if cache is valid
        ItemStockCache itemStockCache = stockCacheService.getAvailableStock(userId, saleItem.getId());
        if (itemStockCache != null && itemStockCache.isSuccess() && itemStockCache.getAvailableStock() != null) {
            saleItem.setAvailableStock(itemStockCache.getAvailableStock());
        }
    }

    @Override
    public AppMultiResult<SaleItemDTO> listItems(Long userId, Long activityId, SaleItemsQuery saleItemsQuery) {
        log.info("App listItems: {},{},{}", userId, activityId, saleItemsQuery);
        if (saleItemsQuery == null) {
            log.info("App listItems empty: {},{},{}", userId, activityId, null);
            return AppMultiResult.empty();
        }
        saleItemsQuery.setActivityId(activityId);

        List<SaleItem> items;
        Integer total;
        if (saleItemsQuery.isOnlineFirstPageQuery()) {
            // Set values from cache if it is first page query
            SaleItemsCache saleItemsCache = fssItemsCacheService.getItemsCache(activityId, saleItemsQuery.getVersion());
            if (saleItemsCache.isLater()) {
                log.info("App listItems tryLater: {},{},{}", userId, activityId, saleItemsQuery);
                return AppMultiResult.tryLater();
            }
            if (saleItemsCache.isEmpty()) {
                log.info("App listItems empty: {},{},{}", userId, activityId, saleItemsQuery);
                return AppMultiResult.empty();
            }
            items = saleItemsCache.getSaleItems();
            total = saleItemsCache.getTotal();
        } else {
            // Otherwise, set values from domain service
            PageQueryCondition condition = FssItemAppConverter.toQuery(saleItemsQuery);
            PageResult<SaleItem> itemsPageResult = fssItemDomainService.getItems(condition);
            items = itemsPageResult.getData();
            total = itemsPageResult.getTotal();
        }

        // Return empty result if no items found
        if (CollectionUtils.isEmpty(items)) {
            log.info("App listItems ok: {},{},{}", userId, activityId, saleItemsQuery);
            return AppMultiResult.empty();
        }

        // Convert to DTOs
        List<SaleItemDTO> saleItemDTOS = items.stream().map(FssItemAppConverter::toDTO).collect(Collectors.toList());
        log.info("App listItems ok: {}, {}, {}", userId, activityId, saleItemsQuery);
        return AppMultiResult.of(saleItemDTOS, total);
    }

    @Override
    public AppResult publishItem(Long userId, Long activityId, PublishItemCommand publishItemCommand) {
        log.info("App publishItem: {},{},{}", userId, activityId, publishItemCommand);
        if (userId == null || activityId == null || publishItemCommand == null || publishItemCommand.invalidParams()) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildCreateLockKey(userId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLocked = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Get activity and validate
            SaleActivity saleActivity = fssActivityDomainService.getActivity(activityId);
            if (saleActivity == null) {
                throw new BizException(AppErrCode.ACTIVITY_NOT_FOUND);
            }

            // Publish item
            SaleItem saleItem = FssItemAppConverter.toDomainModel(publishItemCommand);
            saleItem.setActivityId(activityId);
            saleItem.setStockWarmUp(0);
            fssItemDomainService.publishItem(saleItem);

            log.info("App publishItem ok: {},{},{}", userId, activityId, publishItemCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App publishItem failed: {},{},{}", userId, activityId, publishItemCommand, e);
            throw new BizException(AppErrCode.ITEM_PUBLISH_FAILED);
        } finally {
            // Release lock
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult onlineItem(Long userId, Long activityId, Long itemId) {
        log.info("App onlineItem: {},{},{}", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock itemModificationLock = distributedLockService.getDistributedLock(buildModificationLockKey(userId));
        try {
            // Try to acquire lock, wait for 500 milliseconds, timeout is 1000 milliseconds
            boolean isLocked = itemModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Online item
            fssItemDomainService.onlineItem(itemId);

            log.info("App onlineItem ok: {},{},{}", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App onlineItem failed: {},{},{}", userId, activityId, itemId, e);
            throw new BizException(AppErrCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            // Release lock
            itemModificationLock.unlock();
        }
    }

    @Override
    public AppResult offlineItem(Long userId, Long activityId, Long itemId) {
        log.info("App offlineItem: {},{},{}", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock itemModificationLock = distributedLockService.getDistributedLock(buildModificationLockKey(userId));
        try {
            boolean isLocked = itemModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.LOCK_FAILED);
            }

            // Offline item
            fssItemDomainService.offlineItem(itemId);

            log.info("App offlineItem ok: {},{},{}", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App offlineItem failed: {},{},{}", userId, activityId, itemId, e);
            throw new BizException(AppErrCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            // Release lock
            itemModificationLock.unlock();
        }
    }

    @Override
    public boolean isPlaceOrderAllowed(Long itemId) {
        SaleItemCache saleItemCache = fssItemCacheService.getItemCache(itemId, null);
        if (saleItemCache.isLater()) {
            log.info("App isPlaceOrderAllowed tryLater: {}", itemId);
            return false;
        }
        if (!saleItemCache.isExist() || saleItemCache.getSaleItem() == null) {
            log.info("App isPlaceOrderAllowed item not found: {}", itemId);
            return false;
        }
        if (!saleItemCache.getSaleItem().isOnline()) {
            log.info("App isPlaceOrderAllowed item not online: {}", itemId);
            return false;
        }
        if (!saleItemCache.getSaleItem().isInProgress()) {
            log.info("App isPlaceOrderAllowed item not in progress: {}", itemId);
            return false;
        }
        return true;
    }

    private String buildCreateLockKey(Long userId) {
        return link(ITEM_CREATE_LOCK_KEY, userId);
    }

    private String buildModificationLockKey(Long itemId) {
        return link(ITEM_MODIFICATION_LOCK_KEY, itemId);
    }
}
