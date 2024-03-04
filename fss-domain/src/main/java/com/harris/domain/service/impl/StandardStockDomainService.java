package com.harris.domain.service.impl;

import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.repository.SaleItemRepository;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued", matchIfMissing = true)
public class StandardStockDomainService implements StockDomainService {
    @Resource
    private SaleItemRepository saleItemRepository;
    
    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        
        // 从仓库中扣减库存
        boolean res = saleItemRepository.deductStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
        log.info("领域层服务 deductStock, 从仓库中扣减库存: [stockDeduction={}, res={}]", stockDeduction, res);
        return res;
    }
    
    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        
        // 从仓库中恢复库存
        boolean res = saleItemRepository.revertStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
        log.info("领域层服务 revertStock, 从仓库中恢复库存: [stockDeduction={}, res={}]", stockDeduction, res);
        return res;
    }
}
