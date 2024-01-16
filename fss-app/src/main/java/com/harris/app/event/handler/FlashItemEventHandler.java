package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.app.service.cache.FlashItemCacheService;
import com.harris.app.service.cache.FlashItemsCacheService;
import com.harris.domain.event.flashItem.FlashItemEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler
public class FlashItemEventHandler implements EventHandlerI<Response, FlashItemEvent> {
    @Resource
    private FlashItemCacheService flashItemCacheService;

    @Resource
    private FlashItemsCacheService flashItemsCacheService;

    @Override
    public Response execute(FlashItemEvent flashItemEvent) {
        log.info("FlashItemEventHandler: {}", JSON.toJSON(flashItemEvent));
        if (flashItemEvent.getId() == null) {
            log.info("FlashItemEventHandler, invalid params");
            return Response.buildSuccess();
        }
        flashItemCacheService.tryUpdateItemCacheByLock(flashItemEvent.getId());
        flashItemsCacheService.tryUpdateItemsCacheByLock(flashItemEvent.getFlashActivityId());
        return Response.buildSuccess();
    }
}
