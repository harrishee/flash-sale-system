package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.harris.app.service.cache.SaleActivitiesCacheService;
import com.harris.app.service.cache.SaleActivityCacheService;
import com.harris.domain.model.event.SaleActivityEvent;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler // 事件处理器，用于处理销售活动相关的事件
public class SaleActivityEventHandler implements EventHandlerI<Response, SaleActivityEvent> {
    @Resource
    private SaleActivityCacheService saleActivityCacheService;
    
    @Resource
    private SaleActivitiesCacheService saleActivitiesCacheService;
    
    @Override
    @MarkTrace
    public Response execute(SaleActivityEvent saleActivityEvent) {
        if (saleActivityEvent.getActivityId() == null) {
            log.info("应用层 activityEvent，事件参数错误: [saleActivityEvent={}]", saleActivityEvent);
            return Response.buildSuccess();
        }
        
        // 比如接收到领域层的 活动发布 / 活动修改 / 活动上线 / 活动下线 等事件，更新分布式缓存
        
        // 更新此事件对应的 活动缓存 和 第一页活动列表缓存
        saleActivityCacheService.tryUpdateDistActivityCache(saleActivityEvent.getActivityId());
        saleActivitiesCacheService.tryUpdateDistActivitiesCache(1);
        return Response.buildSuccess();
    }
}
