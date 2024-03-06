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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ItemPreheatScheduler {
    @Resource
    private SaleItemDomainService saleItemDomainService;
    @Resource
    private StockCacheService stockCacheService;
    
    @MarkTrace
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 固定每 6 小时执行一次这个方法
    public void itemPreheatTask() {
        log.info("定时任务 itemPreheatTask，商品预热开始");
        
        // TODO: 预热时可以进一步根据秒杀品的上线时间进行预热，只预热临近开始的秒杀品；
        // TODO: 预热过程中，应对已预热但即将开始的活动进行预热复查，防止数据库中显示已预热，但缓存中的数据已经丢失；
        
        // 调用领域服务获取所有 未预热 的商品列表
        PageQuery pageQuery = new PageQuery();
        pageQuery.setStockWarmUp(0);
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQuery);
        List<Long> ids = new ArrayList<>();
        
        pageResult.getData().forEach(saleItem -> {
            // 确保缓存中的库存和数据库中的库存保持一致
            boolean initSuccess = stockCacheService.syncCachedStockToDB(saleItem.getId());
            if (!initSuccess) {
                log.info("定时任务 itemPreheatTask，商品初始化预热失败: [saleItemId={}]", saleItem.getId());
                return;
            }
            
            // 预热成功后，将商品的预热状态设置为 1，并发布商品
            saleItem.setStockWarmUp(1);
            saleItemDomainService.publishItem(saleItem);
            ids.add(saleItem.getId());
        });
        
        log.info("定时任务 itemPreheatTask，商品预热完成: [total={}, itemIds={}]", pageResult.getTotal(), ids);
    }
}
