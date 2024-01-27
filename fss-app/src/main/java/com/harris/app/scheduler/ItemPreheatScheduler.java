package com.harris.app.scheduler;

import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
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
    public void itemPreheatTask() {
        log.info("ItemPreheatScheduler starts");
        PageQuery pageQuery = new PageQuery();
        pageQuery.setStockWarmUp(0);
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQuery);

        // Iterate through the sale items
        pageResult.getData().forEach(saleItem -> {
            // Initialize the stock in the cache
            boolean initSuccess = stockCacheService.alignStock(saleItem.getId());
            if (!initSuccess) {
                log.info("Item init preheat failed: {}", saleItem.getId());
                return;
            }

            // Set the stock warm-up status and publish the item
            saleItem.setStockWarmUp(1);
            saleItemDomainService.publishItem(saleItem);
            log.info("Item init preheat success: {}", saleItem.getId());
        });
    }
}
