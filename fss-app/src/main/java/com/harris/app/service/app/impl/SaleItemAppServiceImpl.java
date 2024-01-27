package com.harris.app.service.app.impl;

import com.harris.app.exception.AppErrorCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.auth.ResourceEnum;
import com.harris.app.model.cache.SaleItemCache;
import com.harris.app.model.cache.SaleItemsCache;
import com.harris.app.model.cache.StockCache;
import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.converter.SaleItemAppConverter;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.AuthAppService;
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
import com.harris.infra.util.LinkUtil;
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
    private AuthAppService authAppService;

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
        log.info("App getItem: {}", itemId);
        if (itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Retrieve item from cache and validate
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, null);
        if (itemCache.isLater()) {
            log.info("App getItem tryLater: {}", itemId);
            return AppSingleResult.tryLater();
        }

        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            throw new BizException(AppErrorCode.ITEM_NOT_FOUND.getErrDesc());
        }

        // Update the latest item stock
        updateLatestItemStock(null, itemCache.getSaleItem());
        SaleItemDTO saleItemDTO = SaleItemAppConverter.toDTO(itemCache.getSaleItem());
        saleItemDTO.setVersion(itemCache.getVersion());

        log.info("App getItem ok: {}", itemId);
        return AppSingleResult.ok(saleItemDTO);
    }

    @Override
    public AppSingleResult<SaleItemDTO> getItem(Long userId, Long activityId, Long itemId, Long version) {
        log.info("App getItem: {},{},{}", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Retrieve item from cache and validate
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, version);
        if (itemCache.isLater()) {
            log.info("App getItem tryLater: {},{},{}", userId, activityId, itemId);
            return AppSingleResult.tryLater();
        }

        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            throw new BizException(AppErrorCode.ITEM_NOT_FOUND.getErrDesc());
        }

        // Update the latest item stock
        updateLatestItemStock(userId, itemCache.getSaleItem());
        SaleItemDTO saleItemDTO = SaleItemAppConverter.toDTO(itemCache.getSaleItem());
        saleItemDTO.setVersion(itemCache.getVersion());

        log.info("App getItem ok: {},{},{}", userId, activityId, itemId);
        return AppSingleResult.ok(saleItemDTO);
    }

    private void updateLatestItemStock(Long userId, SaleItem saleItem) {
        if (saleItem == null) {
            return;
        }

        // Get the available item stock cache, and update the available stock if cache is valid
        StockCache stockCache = stockCacheService.getStockCache(userId, saleItem.getId());
        if (stockCache != null && stockCache.isSuccess() && stockCache.getAvailableStockQuantity() != null) {
            saleItem.setAvailableStock(stockCache.getAvailableStockQuantity());
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
            SaleItemsCache itemsCache = saleItemsCacheService.getItemsCache(activityId, saleItemsQuery.getVersion());
            if (itemsCache.isLater()) {
                log.info("App listItems tryLater: {},{},{}", userId, activityId, saleItemsQuery);
                return AppMultiResult.tryLater();
            }

            if (itemsCache.isEmpty()) {
                log.info("App listItems empty: {},{},{}", userId, activityId, saleItemsQuery);
                return AppMultiResult.empty();
            }

            items = itemsCache.getSaleItems();
            total = itemsCache.getTotal();
        } else {
            // Otherwise, set values from domain service
            PageQuery condition = SaleItemAppConverter.toPageQuery(saleItemsQuery);
            PageResult<SaleItem> itemsPageResult = saleItemDomainService.getItems(condition);

            items = itemsPageResult.getData();
            total = itemsPageResult.getTotal();
        }

        // Return empty result if no items found
        if (CollectionUtils.isEmpty(items)) {
            log.info("App listItems ok: {},{},{}", userId, activityId, saleItemsQuery);
            return AppMultiResult.empty();
        }

        // Convert to DTOs
        List<SaleItemDTO> saleItemDTOS = items.stream().map(SaleItemAppConverter::toDTO).collect(Collectors.toList());
        log.info("App listItems ok: {},{},{}", userId, activityId, saleItemsQuery);
        return AppMultiResult.of(saleItemDTOS, total);
    }

    @Override
    public AppResult publishItem(Long userId, Long activityId, PublishItemCommand publishItemCommand) {
        log.info("App publishItem: {},{},{}", userId, activityId, publishItemCommand);
        if (userId == null || activityId == null || publishItemCommand == null || publishItemCommand.invalidParams()) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildCreateLockKey(userId));
        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Get activity and validate
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            if (saleActivity == null) {
                throw new BizException(AppErrorCode.ACTIVITY_NOT_FOUND);
            }

            // Publish item
            SaleItem saleItem = SaleItemAppConverter.toDomainModel(publishItemCommand);
            saleItem.setActivityId(activityId);
            saleItem.setStockWarmUp(0);
            saleItemDomainService.publishItem(saleItem);

            log.info("App publishItem ok: {},{},{}", userId, activityId, publishItemCommand);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App publishItem failed: {},{},{}", userId, activityId, publishItemCommand, e);
            throw new BizException(AppErrorCode.ITEM_PUBLISH_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult onlineItem(Long userId, Long activityId, Long itemId) {
        log.info("App onlineItem: {},{},{}", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationLockKey(userId));
        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Online item
            saleItemDomainService.onlineItem(itemId);

            log.info("App onlineItem ok: {},{},{}", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App onlineItem failed: {},{},{}", userId, activityId, itemId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public AppResult offlineItem(Long userId, Long activityId, Long itemId) {
        log.info("App offlineItem: {},{},{}", userId, activityId, itemId);
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(AppErrorCode.INVALID_PARAMS);
        }

        // Authenticate user
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Get distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildModificationLockKey(userId));
        try {
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                throw new BizException(AppErrorCode.LOCK_FAILED);
            }

            // Offline item
            saleItemDomainService.offlineItem(itemId);

            log.info("App offlineItem ok: {},{},{}", userId, activityId, itemId);
            return AppResult.ok();
        } catch (Exception e) {
            log.error("App offlineItem failed: {},{},{}", userId, activityId, itemId, e);
            throw new BizException(AppErrorCode.ACTIVITY_MODIFY_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }

    @Override
    public boolean isPlaceOrderAllowed(Long itemId) {
        SaleItemCache itemCache = saleItemCacheService.getItemCache(itemId, null);
        if (itemCache.isLater()) {
            log.info("App isPlaceOrderAllowed tryLater: {}", itemId);
            return false;
        }
        if (!itemCache.isExist() || itemCache.getSaleItem() == null) {
            log.info("App isPlaceOrderAllowed item not found: {}", itemId);
            return false;
        }
        if (!itemCache.getSaleItem().isOnline()) {
            log.info("App isPlaceOrderAllowed item not online: {}", itemId);
            return false;
        }
        if (!itemCache.getSaleItem().isInProgress()) {
            log.info("App isPlaceOrderAllowed item not in progress: {}", itemId);
            return false;
        }

        return true;
    }

    private String buildCreateLockKey(Long userId) {
        return LinkUtil.link(ITEM_CREATE_LOCK_KEY, userId);
    }

    private String buildModificationLockKey(Long itemId) {
        return LinkUtil.link(ITEM_MODIFICATION_LOCK_KEY, itemId);
    }
}
