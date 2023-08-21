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
     * 定义限流规则
     * 1.创建存放限流规则的集合
     * 2.创建限流规则
     * 3.将限流规则放到集合中
     * 4.加载限流规则
     *
     * @PostConstruct 当前类的构造函数执行完之后执行
     */
    @PostConstruct
    public void seckillsFlow() {

        List<FlowRule> rules = new ArrayList<>();

        // 2.创建限流规则
        FlowRule rule = new FlowRule();
        rule.setResource("activityAll");           // 定义资源，表示 sentinel 会对那个资源生效
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 定义限流规则类型,QPS 类型
        rule.setCount(1);                           // 定义 QPS 每秒通过的请求数

        // 3.将限流规则放到集合中
        rules.add(rule);

        // 4.加载限流规则
        FlowRuleManager.loadRules(rules);

        log.info("***Sentinel*** 限流规则加载成功");
    }
}
