package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.app.service.cache.FssItemCacheService;
import com.harris.app.service.cache.FssItemsCacheService;
import com.harris.domain.event.flashItem.FlashItemEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler
public class FlashItemEventHandler implements EventHandlerI<Response, FlashItemEvent> {
    @Resource
    private FssItemCacheService fssItemCacheService;

    @Resource
    private FssItemsCacheService fssItemsCacheService;

    @Override
    public Response execute(FlashItemEvent flashItemEvent) {
        log.info("FlashItemEventHandler: {}", JSON.toJSON(flashItemEvent));
        if (flashItemEvent.getId() == null) {
            log.info("FlashItemEventHandler, invalid params");
            return Response.buildSuccess();
        }
        fssItemCacheService.tryUpdateItemCacheByLock(flashItemEvent.getId());
        fssItemsCacheService.tryUpdateItemsCacheByLock(flashItemEvent.getFlashActivityId());
        return Response.buildSuccess();
    }
}
