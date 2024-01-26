package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.app.service.cache.FssItemCacheService;
import com.harris.app.service.cache.FssItemsCacheService;
import com.harris.domain.model.event.SaleItemEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler
public class FlashItemEventHandler implements EventHandlerI<Response, SaleItemEvent> {
    @Resource
    private FssItemCacheService fssItemCacheService;

    @Resource
    private FssItemsCacheService fssItemsCacheService;

    @Override
    public Response execute(SaleItemEvent saleItemEvent) {
        log.info("FlashItemEventHandler: {}", JSON.toJSON(saleItemEvent));
        if (saleItemEvent.getId() == null) {
            log.info("FlashItemEventHandler, invalid params");
            return Response.buildSuccess();
        }
        fssItemCacheService.tryUpdateItemCacheByLock(saleItemEvent.getId());
        fssItemsCacheService.tryUpdateItemsCacheByLock(saleItemEvent.getActivityId());
        return Response.buildSuccess();
    }
}
