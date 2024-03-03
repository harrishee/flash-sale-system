package com.harris.domain.service;

import com.harris.domain.model.StockDeduction;

/**
 * 库存领域服务
 * 将会有两种库存服务：
 * 1. 常规库村服务
 * 2. 待定
 */
public interface StockDomainService {
    // 根据 库存扣减信息 扣减库存
    boolean deductStock(StockDeduction stockDeduction);
    
    // 根据 库存扣减信息 恢复库存
    boolean revertStock(StockDeduction stockDeduction);
}
