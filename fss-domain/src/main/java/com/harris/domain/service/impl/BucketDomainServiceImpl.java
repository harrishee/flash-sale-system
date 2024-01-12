package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrCode;
import com.harris.domain.event.bucket.BucketEventType;
import com.harris.domain.event.bucket.BucketEvent;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.Bucket;
import com.harris.domain.repository.BucketsRepository;
import com.harris.domain.service.BucketDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "buckets", matchIfMissing = true)
public class BucketDomainServiceImpl implements BucketDomainService {
    @Resource
    private BucketsRepository bucketsRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public List<Bucket> getBucketsByItemId(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }
        return bucketsRepository.findBucketsByItemId(itemId);
    }

    @Override
    public boolean arrangeBucketsByItemId(Long itemId, List<Bucket> buckets) {
        log.info("arrangeBuckets TRY: {},{}", itemId, JSON.toJSONString(buckets));
        if (itemId == null || itemId <= 0 || CollectionUtils.isEmpty(buckets)) {
            log.error("arrangeBuckets" + DomainErrCode.INVALID_PARAMS);
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }

        Optional<Bucket> primaryBuckets = buckets.stream().filter(Bucket::isPrimary).findFirst();
        if (!primaryBuckets.isPresent()) {
            throw new DomainException(DomainErrCode.PRIMARY_BUCKET_IS_MISSING);
        }
        if (buckets.stream().filter(Bucket::isPrimary).count() > 1) {
            throw new DomainException(DomainErrCode.MULTI_PRIMARY_BUCKETS_FOUND_BUT_EXPECT_ONE);
        }

        buckets.forEach(stockBucket -> {
            if (stockBucket.getTotalStocksAmount() == null || stockBucket.getTotalStocksAmount() < 0) {
                throw new DomainException(DomainErrCode.TOTAL_STOCKS_AMOUNT_INVALID);
            }
            if (stockBucket.getAvailableStocksAmount() == null || stockBucket.getAvailableStocksAmount() <= 0) {
                throw new DomainException(DomainErrCode.AVAILABLE_STOCKS_AMOUNT_INVALID);
            }
            if (!stockBucket.getAvailableStocksAmount().equals(stockBucket.getTotalStocksAmount()) && !stockBucket.isPrimary()) {
                throw new DomainException(DomainErrCode.AVAILABLE_STOCKS_AMOUNT_NOT_EQUALS_TO_TOTAL_STOCKS_AMOUNT);
            }
            if (!itemId.equals(stockBucket.getItemId())) {
                throw new DomainException(DomainErrCode.STOCK_BUCKET_ITEM_INVALID);
            }
        });

        boolean success = bucketsRepository.saveBuckets(itemId, buckets);
        if (!success) {
            return false;
        }
        BucketEvent bucketEvent = new BucketEvent();
        bucketEvent.setBucketEventType(BucketEventType.ARRANGED);
        bucketEvent.setItemId(itemId);
        domainEventPublisher.publish(bucketEvent);
        log.info("arrangeBuckets DONE: {}", itemId);
        return true;
    }

    @Override
    public boolean suspendBuckets(Long itemId) {
        log.info("suspendBuckets TRY: {}", itemId);
        if (itemId == null || itemId <= 0) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }

        boolean success = bucketsRepository.suspendBucketsByItemId(itemId);
        if (!success) {
            return false;
        }
        BucketEvent bucketEvent = new BucketEvent();
        bucketEvent.setBucketEventType(BucketEventType.DISABLED);
        bucketEvent.setItemId(itemId);
        domainEventPublisher.publish(bucketEvent);
        log.info("suspendBuckets DONE: {}", itemId);
        return true;
    }

    @Override
    public boolean resumeBuckets(Long itemId) {
        log.info("resumeBuckets TRY: {}", itemId);
        if (itemId == null || itemId <= 0) {
            throw new DomainException(DomainErrCode.INVALID_PARAMS);
        }

        boolean success = bucketsRepository.resumeBucketsByItemId(itemId);
        if (!success) {
            return false;
        }
        BucketEvent bucketEvent = new BucketEvent();
        bucketEvent.setBucketEventType(BucketEventType.ENABLED);
        bucketEvent.setItemId(itemId);
        domainEventPublisher.publish(bucketEvent);
        log.info("resumeBuckets DONE: {}", itemId);
        return true;
    }
}
