package com.harris.app.event.handler;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import com.harris.domain.event.bucket.BucketEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EventHandler
public class BucketsEventHandler implements EventHandlerI<Response, BucketEvent> {
    @Override
    public Response execute(BucketEvent bucketEvent) {
        log.info("BucketsEventHandler: {}", JSON.toJSON(bucketEvent));
        return Response.buildSuccess();
    }
}
