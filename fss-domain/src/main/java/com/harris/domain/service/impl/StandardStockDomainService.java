package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
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
@ConditionalOnProperty(name = "place_order_type", havingValue = "standard", matchIfMissing = true)
public class StandardStockDomainService implements StockDomainService {
    @Resource
    private SaleItemRepository saleItemRepository;

    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        log.info("Standard deductStock: {}", JSON.toJSONString(stockDeduction));

        // Validate params
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        return saleItemRepository.deductStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }

    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        log.info("Standard revertStock: {}", JSON.toJSONString(stockDeduction));

        // Validate params
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        return saleItemRepository.revertStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }
}
