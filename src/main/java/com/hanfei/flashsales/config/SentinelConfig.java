package com.hanfei.flashsales.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Component
public class SentinelConfig {

    /**
     * Define rate limiting rules:
     * 1. Create a collection to store rate limiting rules
     * 2. Create rate limiting rules
     * 3. Add rate limiting rules to the collection
     * 4. Load rate limiting rules
     *
     * @PostConstruct Executed after the constructor of this class
     */
    @PostConstruct
    public void seckillsFlow() {

        List<FlowRule> rules = new ArrayList<>();

        // 2. Create rate limiting rules
        FlowRule rule = new FlowRule();
        // Define the resource for which Sentinel applies rate limiting
        rule.setResource("activityAll");
        // Define the rate limiting rule type (QPS type)
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Define the number of requests allowed per second (QPS)
        rule.setCount(1);

        // 3. Add rate limiting rules to the collection
        rules.add(rule);

        // 4. Load rate limiting rules
        FlowRuleManager.loadRules(rules);

        log.info("Sentinel rate limiting rules loaded success!");
    }
}
