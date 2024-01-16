package com.harris.app.util.impl;

import com.harris.app.model.OrderNoContext;
import com.harris.app.util.OrderNoService;
import com.harris.infra.util.SnowflakeIdWorker;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class SnowflakeOrderNoService implements OrderNoService {
    private SnowflakeIdWorker snowflakeIdWorker;

    @PostConstruct
    public void initWorker() {
        Random random = new Random(1);
        snowflakeIdWorker = new SnowflakeIdWorker(random.nextInt(32), 1, 1);
    }

    @Override
    public Long generateOrderNo(OrderNoContext orderNoContext) {
        return snowflakeIdWorker.nextId();
    }
}
