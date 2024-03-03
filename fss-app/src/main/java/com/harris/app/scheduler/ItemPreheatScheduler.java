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
    private SaleItemDomainService saleItemDomainService; // 注入商品领域服务，用于获取和更新商品信息
    @Resource
    private StockCacheService stockCacheService; // 注入库存缓存服务，用于库存的预热和对齐
    
    @MarkTrace
    @Scheduled(cron = "*/5 * * * * ?") // 定时任务，每 5 秒执行一次这个方法
    public void itemPreheatTask() {
        // log.info("应用层 itemPreheatTask，商品预热调度");
        
        // 创建一个查询对象，设置库存预热标志为 0（表示需要预热的商品）
        PageQuery pageQuery = new PageQuery();
        pageQuery.setStockWarmUp(0);
        
        // 调用领域服务获取需要预热的商品列表
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQuery);
        
        // 遍历商品列表，对每个商品执行预热操作
        pageResult.getData().forEach(saleItem -> {
            // 调用库存缓存服务对商品库存进行预热，返回预热是否成功
            boolean initSuccess = stockCacheService.alignStock(saleItem.getId());
            if (!initSuccess) {
                log.info("应用层 itemPreheatTask，商品初始化预热失败: {}", saleItem.getId());
                return;
            }
            
            // 如果预热成功，更新商品的库存预热标志为1（表示已预热）
            saleItem.setStockWarmUp(1);
            // 调用商品领域服务发布商品
            saleItemDomainService.publishItem(saleItem);
            // log.info("应用层 itemPreheatTask，商品初始化预热成功: {}", saleItem.getId());
        });
    }
}
