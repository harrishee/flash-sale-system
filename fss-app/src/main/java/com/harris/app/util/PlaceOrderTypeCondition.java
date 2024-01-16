package com.harris.app.util;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class PlaceOrderTypeCondition extends AnyNestedCondition {
    public PlaceOrderTypeCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = "place_order_type", havingValue = "standard", matchIfMissing = true)
    static class StandardCondition {
    }

    @ConditionalOnProperty(name = "place_order_type", havingValue = "bucket", matchIfMissing = true)
    static class BucketCondition {
    }
}
