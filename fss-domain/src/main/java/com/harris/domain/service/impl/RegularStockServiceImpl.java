package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.repository.FlashItemRepository;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "regular", matchIfMissing = true)
public class RegularStockServiceImpl implements StockDomainService {
    @Resource
    private FlashItemRepository flashItemRepository;

    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        log.info("REGULAR decreaseItemStock: {}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        return flashItemRepository.deductStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }

    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        log.info("REGULAR increaseItemStock: {}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        return flashItemRepository.revertStockForItem(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }
}
