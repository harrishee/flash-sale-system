package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.domain.event.flashOrder.FlashOrderEvent;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EventHandler
public class FlashOrderEventHandler implements EventHandlerI<Response, FlashOrderEvent> {
    @Override
    @MarkTrace
    public Response execute(FlashOrderEvent flashOrderEvent) {
        log.info("FlashOrderEventHandler: {}", JSON.toJSON(flashOrderEvent));
        if (flashOrderEvent.getOrderId() == null) {
            log.info("FlashOrderEventHandler, invalid params");
            return Response.buildSuccess();
        }
        return Response.buildSuccess();
    }
}
