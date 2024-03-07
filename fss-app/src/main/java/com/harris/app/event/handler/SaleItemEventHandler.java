package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.harris.app.service.saleitem.SaleItemCacheService;
import com.harris.app.service.saleitem.SaleItemsCacheService;
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
        if (saleItemEvent.getItemId() == null) {
            log.info("应用层 item event handler，事件参数ID为空: [saleItemEvent={}]", saleItemEvent);
            return Response.buildSuccess();
        }
        
        // 比如接收到领域层的 商品发布 / 商品上线 / 商品下线 等事件，更新分布式缓存
        
        // 更新此事件对应的 商品缓存 和 同一活动下的商品列表缓存
        saleItemCacheService.tryUpdateItemCache(saleItemEvent.getItemId());
        saleItemsCacheService.tryUpdateItemsCache(saleItemEvent.getActivityId());
        return Response.buildSuccess();
    }
}
