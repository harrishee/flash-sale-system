package com.harris.app.scheduler;

import com.harris.app.service.cache.ItemStockCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.model.enums.FlashItemStatus;
import com.harris.domain.service.FlashItemDomainService;
import com.harris.infra.config.annotation.MarkTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StocksAlignScheduler {
    @Resource
    private FlashItemDomainService flashItemDomainService;
    
    @Resource
    private ItemStockCacheService itemStockCacheService;

    @MarkTrace
    @Scheduled(cron = "*/2 * * * * ?")
    public void alignStocksTask() {
        log.info("StocksAlignScheduler starts");
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        pagesQueryCondition.setStatus(FlashItemStatus.ONLINE.getCode());
        PageResult<FlashItem> pageResult = flashItemDomainService.getItems(pagesQueryCondition);

        // Iterate through the flash items
        pageResult.getData().forEach(flashItem -> {
            // Align the item stocks in the cache
            boolean result = itemStockCacheService.alignItemStocks(flashItem.getId());
            if (!result) {
                log.info("StocksAlignScheduler, align failed: {},{}", flashItem.getId(), flashItem.getAvailableStock());
                return;
            }
            log.info("StocksAlignScheduler, align success: {},{}", flashItem.getId(), flashItem.getAvailableStock());
        });
        log.info("StocksAlignScheduler DONE");
    }
}
