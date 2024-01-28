package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.harris.app.service.cache.SaleActivitiesCacheService;
import com.harris.app.service.cache.SaleActivityCacheService;
import com.harris.domain.model.event.SaleActivityEvent;
import com.harris.infra.config.MarkTrace;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import static com.harris.app.exception.AppErrorCode.INVALID_PARAMS;

@Slf4j
@EventHandler
public class SaleActivityEventHandler implements EventHandlerI<Response, SaleActivityEvent> {
    @Resource
    private SaleActivityCacheService saleActivityCacheService;

    @Resource
    private SaleActivitiesCacheService saleActivitiesCacheService;

    @Override
    @MarkTrace
    public Response execute(SaleActivityEvent saleActivityEvent) {
        log.info("SaleActivityEventHandler: {}", saleActivityEvent);

        if (saleActivityEvent.getId() == null) {
            log.info("SaleActivityEventHandler, invalid params");
            return Response.buildFailure(INVALID_PARAMS.getErrCode(), INVALID_PARAMS.getErrDesc());
        }

        // Update activity to Redis distributed cache
        saleActivityCacheService.tryUpdateActivityCache(saleActivityEvent.getId());
        saleActivitiesCacheService.tryUpdateActivitiesCache(1);

        return Response.buildSuccess();
    }
}
