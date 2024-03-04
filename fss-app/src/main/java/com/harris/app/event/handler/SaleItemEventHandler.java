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
@EventHandler // 事件处理器，用于处理销售商品相关的事件
public class SaleItemEventHandler implements EventHandlerI<Response, SaleItemEvent> {
    @Resource
    private SaleItemCacheService saleItemCacheService;

    @Resource
    private SaleItemsCacheService saleItemsCacheService;

    @Override
    public Response execute(SaleItemEvent saleItemEvent) {
        log.info("应用层 itemEvent，接收商品事件: {}", JSON.toJSON(saleItemEvent));

        if (saleItemEvent.getItemId() == null) {
            log.info("应用层 itemEvent，商品事件参数错误");
            return Response.buildSuccess();
        }
        
        // 调用商品缓存服务尝试更新指定商品ID的缓存
        saleItemCacheService.tryUpdateItemCache(saleItemEvent.getItemId());
        
        // 调用商品缓存服务尝试更新指定活动ID的商品缓存
        saleItemsCacheService.tryUpdateItemsCache(saleItemEvent.getActivityId());
        
        return Response.buildSuccess();
    }
}
