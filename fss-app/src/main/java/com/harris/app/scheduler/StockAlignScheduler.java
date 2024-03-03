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
    private SaleItemDomainService saleItemDomainService; // 注入商品领域服务，用于获取商品信息
    
    @Resource
    private StockCacheService stockCacheService; // 注入库存缓存服务，用于校准库存数据
    
    @MarkTrace
    @Scheduled(cron = "*/2 * * * * ?") // 定时任务，每 2 秒执行一次这个方法
    public void alignStockTask() {
        // log.info("应用层 alignStockTask，校准库存缓存开始");
        
        // 调用领域服务获取在线商品列表
        PageQuery pageQuery = new PageQuery();
        pageQuery.setStatus(SaleItemStatus.ONLINE.getCode());
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQuery);
        
        // 遍历商品列表，对每个商品执行库存校准操作
        pageResult.getData().forEach(saleItem -> {
            // 调用库存缓存服务进行库存校准，返回校准是否成功
            boolean result = stockCacheService.alignStock(saleItem.getId());
            if (!result) {
                log.info("应用层 alignStockTask，校准库存失败: {},{}", saleItem.getId(), saleItem.getAvailableStock());
                return;
            }
            // log.info("应用层 alignStockTask，校准库存完成: {},{}", saleItem.getId(), saleItem.getAvailableStock());
        });
    }
}
