package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.app.service.cache.FlashActivitiesCacheService;
import com.harris.app.service.cache.FlashActivityCacheService;
import com.harris.domain.event.flashActivity.FlashActivityEvent;
import com.harris.infra.config.annotation.MarkTrace;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler
public class FlashActivityEventHandler implements EventHandlerI<Response, FlashActivityEvent> {
    @Resource
    private FlashActivityCacheService flashActivityCacheService;

    @Resource
    private FlashActivitiesCacheService flashActivitiesCacheService;

    @Override
    @MarkTrace
    public Response execute(FlashActivityEvent flashActivityEvent) {
        log.info("FlashActivityEventHandler: {}", JSON.toJSON(flashActivityEvent));
        if (flashActivityEvent.getId() == null) {
            log.info("FlashActivityEventHandler, invalid params");
            return Response.buildSuccess();
        }
        flashActivityCacheService.tryUpdateActivityCacheByLock(flashActivityEvent.getId());
        flashActivitiesCacheService.tryUpdateActivitiesCacheByLock(1);
        return Response.buildSuccess();
    }
}
