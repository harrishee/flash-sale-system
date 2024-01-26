package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.repository.BucketsRepository;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "bucket", matchIfMissing = true)
public class BucketStockServiceImpl implements StockDomainService {
    @Resource
    private BucketsRepository bucketsRepository;

    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        log.info("BUCKET decreaseItemStock: {}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || stockDeduction.getItemId() == null ||
                stockDeduction.getQuantity() == null || stockDeduction.getSerialNo() == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        return bucketsRepository.deductStockForItem(stockDeduction.getItemId(),
                stockDeduction.getQuantity(), stockDeduction.getSerialNo());
    }

    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        log.info("BUCKET increaseItemStock: {}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || stockDeduction.getItemId() == null ||
                stockDeduction.getQuantity() == null || stockDeduction.getSerialNo() == null) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        return bucketsRepository.revertStockForItem(stockDeduction.getItemId(),
                stockDeduction.getQuantity(), stockDeduction.getSerialNo());
    }
}
