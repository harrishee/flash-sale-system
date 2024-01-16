package com.harris.app.service.main.impl;

import com.harris.app.auth.AuthAppService;
import com.harris.app.auth.model.AuthResult;
import com.harris.app.auth.model.ResourceEnum;
import com.harris.app.exception.AppErrCode;
import com.harris.app.exception.BizException;
import com.harris.app.model.cache.FlashItemCache;
import com.harris.app.model.cache.FlashItemsCache;
import com.harris.app.model.cache.ItemStockCache;
import com.harris.app.model.command.FlashItemPublishCommand;
import com.harris.app.model.converter.FlashItemAppConverter;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.query.FlashItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.cache.FlashItemCacheService;
import com.harris.app.service.cache.FlashItemsCacheService;
import com.harris.app.service.cache.ItemStockCacheService;
import com.harris.app.service.main.FlashItemAppService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.FlashActivity;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.service.FlashActivityDomainService;
import com.harris.domain.service.FlashItemDomainService;
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

import static com.harris.app.exception.AppErrCode.*;
import static com.harris.infra.controller.exception.AuthErrCode.UNAUTHORIZED_ACCESS;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashItemAppServiceImpl implements FlashItemAppService {
    @Resource
    private AuthAppService authAppService;

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Resource
    private FlashItemCacheService flashItemCacheService;

    @Resource
    private FlashItemsCacheService flashItemsCacheService;

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    public AppSingleResult<FlashItemDTO> getFlashItem(Long itemId) {
        FlashItemCache flashItemCache = flashItemCacheService.getItemCache(itemId, null);
        if (flashItemCache.isLater()) {
            return AppSingleResult.tryLater();
        }
        if (!flashItemCache.isExist() || flashItemCache.getFlashItem() == null) {
            throw new BizException(ITEM_NOT_FOUND.getErrDesc());
        }
        updateLatestItemStock(null, flashItemCache.getFlashItem());
        FlashItemDTO flashItemDTO = FlashItemAppConverter.toDTO(flashItemCache.getFlashItem());
        flashItemDTO.setVersion(flashItemCache.getVersion());
        return AppSingleResult.ok(flashItemDTO);
    }

    @Override
    public AppSingleResult<FlashItemDTO> getFlashItem(Long userId, Long activityId, Long itemId, Long version) {
        FlashItemCache flashItemCache = flashItemCacheService.getItemCache(itemId, version);
        if (flashItemCache.isLater()) {
            return AppSingleResult.tryLater();
        }
        if (!flashItemCache.isExist() || flashItemCache.getFlashItem() == null) {
            throw new BizException(ITEM_NOT_FOUND.getErrDesc());
        }
        updateLatestItemStock(userId, flashItemCache.getFlashItem());
        FlashItemDTO flashItemDTO = FlashItemAppConverter.toDTO(flashItemCache.getFlashItem());
        flashItemDTO.setVersion(flashItemCache.getVersion());
        return AppSingleResult.ok(flashItemDTO);
    }

    @Override
    public AppMultiResult<FlashItemDTO> getFlashItems(Long userId, Long activityId, FlashItemsQuery flashItemsQuery) {
        if (flashItemsQuery == null) {
            return AppMultiResult.empty();
        }
        flashItemsQuery.setActivityId(activityId);
        List<FlashItem> items;
        Integer total;
        if (flashItemsQuery.isOnlineFirstPageQuery()) {
            FlashItemsCache flashItemsCache = flashItemsCacheService.getItemsCache(activityId, flashItemsQuery.getVersion());
            if (flashItemsCache.isLater()) {
                return AppMultiResult.tryLater();
            }
            if (flashItemsCache.isEmpty()) {
                return AppMultiResult.empty();
            }
            items = flashItemsCache.getFlashItems();
            total = flashItemsCache.getTotal();
        } else {
            PageResult<FlashItem> flashItemPageResult = flashItemDomainService.getItems(FlashItemAppConverter.toQuery(flashItemsQuery));
            items = flashItemPageResult.getData();
            total = flashItemPageResult.getTotal();
        }
        if (CollectionUtils.isEmpty(items)) {
            return AppMultiResult.empty();
        }
        List<FlashItemDTO> flashItemDTOS = items.stream().map(FlashItemAppConverter::toDTO).collect(Collectors.toList());
        return AppMultiResult.of(total, flashItemDTOS);
    }

    @Override
    public AppResult publishFlashItem(Long userId, Long activityId, FlashItemPublishCommand flashItemPublishCommand) {
        if (userId == null || activityId == null || flashItemPublishCommand == null || flashItemPublishCommand.invalidParams()) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock itemCreateLock = distributedLockService.getDistributedLock(buildItemCreateLockKey(userId));
        try {
            boolean isLocked = itemCreateLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(AppErrCode.FREQUENTLY_ERROR);
            }
            FlashActivity flashActivity = flashActivityDomainService.getActivity(activityId);
            if (flashActivity == null) {
                throw new BizException(AppErrCode.ACTIVITY_NOT_FOUND);
            }
            FlashItem flashItem = FlashItemAppConverter.toDomainObj(flashItemPublishCommand);
            flashItem.setActivityId(activityId);
            flashItem.setStockWarmUp(0);
            flashItemDomainService.publishItem(flashItem);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("publishFlashItem failed");
        } finally {
            itemCreateLock.unlock();
        }
    }

    @Override
    public AppResult onlineFlashItem(Long userId, Long activityId, Long itemId) {
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock itemModificationLock = distributedLockService.getDistributedLock(buildItemModificationLockKey(userId));
        try {
            boolean isLocked = itemModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new BizException(LOCK_FAILED_ERROR);
            }
            flashItemDomainService.onlineItem(itemId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("onlineFlashItem failed");
        } finally {
            itemModificationLock.unlock();
        }
    }

    @Override
    public AppResult offlineFlashItem(Long userId, Long activityId, Long itemId) {
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        AuthResult authResult = authAppService.auth(userId, ResourceEnum.ITEM_MODIFICATION);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        DistributedLock itemModificationLock = distributedLockService.getDistributedLock(buildItemModificationLockKey(userId));
        try {
            boolean isLockSuccess = itemModificationLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!isLockSuccess) {
                throw new BizException(LOCK_FAILED_ERROR);
            }
            flashItemDomainService.offlineItem(itemId);
            return AppResult.ok();
        } catch (Exception e) {
            throw new BizException("offlineFlashItem failed");
        } finally {
            itemModificationLock.unlock();
        }
    }

    @Override
    public boolean isPlaceOrderAllowed(Long itemId) {
        FlashItemCache flashItemCache = flashItemCacheService.getItemCache(itemId, null);
        if (flashItemCache.isLater()) {
            log.info("isAllowPlaceOrderOrNot, try later: {}", itemId);
            return false;
        }
        if (!flashItemCache.isExist() || flashItemCache.getFlashItem() == null) {
            log.info("isAllowPlaceOrderOrNot, item not exist: {}", itemId);
            return false;
        }
        if (!flashItemCache.getFlashItem().isOnline()) {
            log.info("isAllowPlaceOrderOrNot, item not online: {}", itemId);
            return false;
        }
        if (!flashItemCache.getFlashItem().isInProgress()) {
            log.info("isAllowPlaceOrderOrNot, item not in progress: {}", itemId);
            return false;
        }
        return true;
    }

    private void updateLatestItemStock(Long userId, FlashItem flashItem) {
        if (flashItem == null) {
            return;
        }
        ItemStockCache itemStockCache = itemStockCacheService.getAvailableItemStock(userId, flashItem.getId());
        if (itemStockCache != null && itemStockCache.isSuccess() && itemStockCache.getAvailableStock() != null) {
            flashItem.setAvailableStock(itemStockCache.getAvailableStock());
        }
    }

    private String buildItemCreateLockKey(Long userId) {
        return link("ITEM_CREATE_LOCK_KEY", userId);
    }

    private String buildItemModificationLockKey(Long itemId) {
        return link("ITEM_MODIFICATION_LOCK_KEY", itemId);
    }
}
