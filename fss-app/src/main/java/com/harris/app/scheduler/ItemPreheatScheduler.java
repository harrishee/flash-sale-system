package com.harris.app.scheduler;

import com.harris.app.service.cache.ItemStockCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.service.FlashItemDomainService;
import com.harris.infra.config.annotation.MarkTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ItemPreheatScheduler {
    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @MarkTrace
    @Scheduled(cron = "*/5 * * * * ?")
    public void warmUpFlashItemTask() {
        log.info("ItemPreheatScheduler starts");
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        pagesQueryCondition.setStockWarmUp(0);
        PageResult<FlashItem> pageResult = flashItemDomainService.getItems(pagesQueryCondition);

        // Iterate through the flash items
        pageResult.getData().forEach(flashItem -> {
            // Initialize the item stocks in the cache
            boolean initSuccess = itemStockCacheService.alignItemStocks(flashItem.getId());
            if (!initSuccess) {
                log.info("Item init preheat failed: {}", flashItem.getId());
                return;
            }

            // Set the stock warm-up status and publish the item
            flashItem.setStockWarmUp(1);
            flashItemDomainService.publishItem(flashItem);
            log.info("Item init preheat success: {}", flashItem.getId());
        });
    }
}
