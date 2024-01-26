package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.repository.BucketRepository;
import com.harris.domain.service.StockDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "bucket", matchIfMissing = true)
public class BucketStockDomainService implements StockDomainService {
    @Resource
    private BucketRepository bucketRepository;

    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        log.info("Bucket deductStock: {}", JSON.toJSONString(stockDeduction));

        // Validate params
        if (stockDeduction == null || stockDeduction.getItemId() == null ||
                stockDeduction.getQuantity() == null || stockDeduction.getSerialNo() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        return bucketRepository.deductStockForItem(stockDeduction.getItemId(),
                stockDeduction.getQuantity(), stockDeduction.getSerialNo());
    }

    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        log.info("Bucket revertStock: {}", JSON.toJSONString(stockDeduction));

        // Validate params
        if (stockDeduction == null || stockDeduction.getItemId() == null ||
                stockDeduction.getQuantity() == null || stockDeduction.getSerialNo() == null) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        return bucketRepository.revertStockForItem(stockDeduction.getItemId(),
                stockDeduction.getQuantity(), stockDeduction.getSerialNo());
    }
}
