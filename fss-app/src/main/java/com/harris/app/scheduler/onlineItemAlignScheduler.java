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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class onlineItemAlignScheduler {
    @Resource
    private SaleItemDomainService saleItemDomainService;
    
    @Resource
    private StockCacheService stockCacheService;
    
    @MarkTrace
    @Scheduled(cron = "*/2 * * * * ?") // 每 2 秒执行一次这个方法
    public void onlineItemAlignTask() {
        // log.info("定时任务 onlineItemAlignTask，在线商品库存校准开始");
        
        // 调用领域服务获取所有 在线 商品列表
        PageQuery pageQuery = new PageQuery();
        pageQuery.setStatus(SaleItemStatus.ONLINE.getCode());
        PageResult<SaleItem> pageResult = saleItemDomainService.getItems(pageQuery);
        List<Long> ids = new ArrayList<>();
        
        pageResult.getData().forEach(saleItem -> {
            // 确保缓存中的库存和数据库中的库存保持一致
            boolean result = stockCacheService.syncCachedStockToDB(saleItem.getId());
            if (!result) log.info("定时任务 onlineItemAlignTask，在线商品库存校准失败: [itemId={}]", saleItem.getId());
            else ids.add(saleItem.getId());
        });
        
        // log.info("定时任务 onlineItemAlignTask，在线商品库存校准完成: [total={}, itemIds={}]", pageResult.getTotal(), ids);
    }
}
