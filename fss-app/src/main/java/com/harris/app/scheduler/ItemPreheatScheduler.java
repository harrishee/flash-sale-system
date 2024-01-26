package com.harris.app.scheduler;

import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.SaleItemDomainService;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ItemPreheatScheduler {
    @Resource
    private SaleItemDomainService saleItemDomainService;

    @Resource
    private StockCacheService stockCacheService;

    @MarkTrace
    @Scheduled(cron = "*/5 * * * * ?")
    public void warmUpFlashItemTask() {
        log.info("ItemPreheatScheduler starts");
        PageQueryCondition pageQueryCondition = new PageQueryCondition();
        pageQueryCondition.setStockWarmUp(0);
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQueryCondition);

        // Iterate through the flash items
        pageResult.getData().forEach(flashItem -> {
            // Initialize the item stocks in the cache
            boolean initSuccess = stockCacheService.alignItemStocks(flashItem.getId());
            if (!initSuccess) {
                log.info("Item init preheat failed: {}", flashItem.getId());
                return;
            }

            // Set the stock warm-up status and publish the item
            flashItem.setStockWarmUp(1);
            saleItemDomainService.publishItem(flashItem);
            log.info("Item init preheat success: {}", flashItem.getId());
        });
    }
}
