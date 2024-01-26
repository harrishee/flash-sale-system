package com.harris.app.scheduler;

import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.service.SaleItemDomainService;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class StocksAlignScheduler {
    @Resource
    private SaleItemDomainService saleItemDomainService;
    
    @Resource
    private StockCacheService stockCacheService;

    @MarkTrace
    @Scheduled(cron = "*/2 * * * * ?")
    public void alignStocksTask() {
        log.info("StocksAlignScheduler starts");
        PageQueryCondition pageQueryCondition = new PageQueryCondition();
        pageQueryCondition.setStatus(SaleItemStatus.ONLINE.getCode());
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQueryCondition);

        // Iterate through the flash items
        pageResult.getData().forEach(flashItem -> {
            // Align the item stocks in the cache
            boolean result = stockCacheService.alignItemStocks(flashItem.getId());
            if (!result) {
                log.info("StocksAlignScheduler, align failed: {},{}", flashItem.getId(), flashItem.getAvailableStock());
                return;
            }
            log.info("StocksAlignScheduler, align success: {},{}", flashItem.getId(), flashItem.getAvailableStock());
        });
        log.info("StocksAlignScheduler DONE");
    }
}
