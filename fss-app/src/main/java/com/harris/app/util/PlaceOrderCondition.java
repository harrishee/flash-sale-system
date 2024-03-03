package com.harris.app.util;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class PlaceOrderCondition extends AnyNestedCondition {
    // 构造函数中指定配置阶段为解析配置阶段，也就是条件的检查会在配置文件被解析后进行
    public PlaceOrderCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }
    
    // 内嵌的条件类，当place_order_type配置属性的值为standard时，此条件满足
    // 如果没有设置place_order_type属性，matchIfMissing属性为true表示条件默认满足
    @ConditionalOnProperty(name = "place_order_type", havingValue = "standard", matchIfMissing = true)
    static class StandardCondition {
    }
}
