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

import static com.harris.app.exception.AppErrorCode.INVALID_PARAMS;

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
        log.info("应用层 activityEvent，接收活动事件: {}", saleActivityEvent);
        
        if (saleActivityEvent.getId() == null) {
            log.info("应用层 activityEvent，事件参数错误");
            return Response.buildFailure(INVALID_PARAMS.getErrCode(), INVALID_PARAMS.getErrDesc());
        }
        
        // 尝试更新指定活动ID的活动缓存
        saleActivityCacheService.tryUpdateActivityCache(saleActivityEvent.getId());
        
        // 尝试更新第一页的活动缓存
        saleActivitiesCacheService.tryUpdateActivitiesCache(1);
        
        return Response.buildSuccess();
    }
}
