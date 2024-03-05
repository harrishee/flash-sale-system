package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.harris.domain.model.event.SaleOrderEvent;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EventHandler // 事件处理器，用于处理销售订单相关的事件
public class SaleOrderEventHandler implements EventHandlerI<Response, SaleOrderEvent> {
    @Override
    @MarkTrace
    public Response execute(SaleOrderEvent saleOrderEvent) {
        // log.info("应用层 orderEvent，接收订单事件: [orderId: {}]", saleOrderEvent.getOrderId());
        
        if (saleOrderEvent.getOrderId() == null) {
            // log.info("应用层 orderEvent，订单事件参数错误");
            return Response.buildSuccess();
        }
        
        return Response.buildSuccess();
    }
}
