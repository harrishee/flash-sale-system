package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.domain.model.event.SaleOrderEvent;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EventHandler
public class SaleOrderEventHandler implements EventHandlerI<Response, SaleOrderEvent> {
    @Override
    @MarkTrace
    public Response execute(SaleOrderEvent saleOrderEvent) {
        log.info("SaleOrderEventHandler: {}", JSON.toJSON(saleOrderEvent));

        if (saleOrderEvent.getOrderId() == null) {
            log.info("SaleOrderEventHandler, invalid params");
            return Response.buildSuccess();
        }

        return Response.buildSuccess();
    }
}
