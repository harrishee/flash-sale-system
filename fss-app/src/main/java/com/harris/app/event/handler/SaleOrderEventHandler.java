package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.harris.domain.model.event.SaleOrderEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EventHandler // 事件处理器，用于处理销售订单相关的事件
public class SaleOrderEventHandler implements EventHandlerI<Response, SaleOrderEvent> {
    @Override
    public Response execute(SaleOrderEvent saleOrderEvent) {
        if (saleOrderEvent.getOrderId() == null) {
            log.info("应用层 order event handler，事件参数ID为空: [saleOrderEvent={}]", saleOrderEvent);
            return Response.buildSuccess();
        }
        
        // 比如接收到领域层的 创建订单 / 取消订单 等事件，这里不做什么处理
        
        return Response.buildSuccess();
    }
}
