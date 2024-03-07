package com.harris.domain.service.stock;

import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.repository.SaleItemRepository;
import com.harris.domain.service.stock.StockDomainService;
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
        return saleItemRepository.deductStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }
    
    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        
        // 从仓库中恢复库存
        return saleItemRepository.revertStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }
}
