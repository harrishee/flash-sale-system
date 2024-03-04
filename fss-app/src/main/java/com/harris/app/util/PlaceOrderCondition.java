package com.harris.app.util;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class PlaceOrderCondition extends AnyNestedCondition {
    // 构造函数中指定配置阶段为解析配置阶段，也就是条件的检查会在配置文件被解析后进行
    public PlaceOrderCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }
    
    @ConditionalOnProperty(name = "place_order_type", havingValue = "standard", matchIfMissing = false)
    static class StandardCondition {
    }
    
    @ConditionalOnProperty(name = "place_order_type", havingValue = "queued", matchIfMissing = true)
    static class QueuedCondition {
    }
}
