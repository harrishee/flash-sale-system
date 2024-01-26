package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.app.service.cache.FssActivitiesCacheService;
import com.harris.app.service.cache.FssActivityCacheService;
import com.harris.domain.model.event.SaleActivityEvent;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler
public class FlashActivityEventHandler implements EventHandlerI<Response, SaleActivityEvent> {
    @Resource
    private FssActivityCacheService fssActivityCacheService;

    @Resource
    private FssActivitiesCacheService fssActivitiesCacheService;

    @Override
    @MarkTrace
    public Response execute(SaleActivityEvent saleActivityEvent) {
        log.info("FlashActivityEventHandler: {}", JSON.toJSON(saleActivityEvent));
        if (saleActivityEvent.getId() == null) {
            log.info("FlashActivityEventHandler, invalid params");
            return Response.buildSuccess();
        }
        fssActivityCacheService.tryUpdateActivityCacheByLock(saleActivityEvent.getId());
        fssActivitiesCacheService.tryUpdateActivitiesCacheByLock(1);
        return Response.buildSuccess();
    }
}
