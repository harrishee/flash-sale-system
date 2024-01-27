package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.app.service.cache.SaleItemCacheService;
import com.harris.app.service.cache.SaleItemsCacheService;
import com.harris.domain.model.event.SaleItemEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@EventHandler
public class SaleItemEventHandler implements EventHandlerI<Response, SaleItemEvent> {
    @Resource
    private SaleItemCacheService saleItemCacheService;

    @Resource
    private SaleItemsCacheService saleItemsCacheService;

    @Override
    public Response execute(SaleItemEvent saleItemEvent) {
        log.info("SaleItemEventHandler: {}", JSON.toJSON(saleItemEvent));

        if (saleItemEvent.getId() == null) {
            log.info("SaleItemEventHandler, invalid params");
            return Response.buildSuccess();
        }

        saleItemCacheService.tryUpdateItemCache(saleItemEvent.getId());
        saleItemsCacheService.tryUpdateItemsCache(saleItemEvent.getActivityId());

        return Response.buildSuccess();
    }
}
