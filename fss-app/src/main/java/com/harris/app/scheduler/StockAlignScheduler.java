package com.harris.app.scheduler;

import com.harris.app.service.cache.StockCacheService;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
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
public class StockAlignScheduler {
    @Resource
    private SaleItemDomainService saleItemDomainService;
    
    @Resource
    private StockCacheService stockCacheService;

    @MarkTrace
    @Scheduled(cron = "*/2 * * * * ?")
    public void alignStockTask() {
        log.info("StockAlignScheduler starts");
        PageQuery pageQuery = new PageQuery();
        pageQuery.setStatus(SaleItemStatus.ONLINE.getCode());
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQuery);

        // Iterate through the sale items
        pageResult.getData().forEach(saleItem -> {
            // Align the stock in the cache
            boolean result = stockCacheService.alignStock(saleItem.getId());
            if (!result) {
                log.info("StockAlignScheduler, align failed: {},{}", saleItem.getId(), saleItem.getAvailableStock());
                return;
            }
            log.info("StockAlignScheduler, align success: {},{}", saleItem.getId(), saleItem.getAvailableStock());
        });
        log.info("StockAlignScheduler DONE");
    }
}
