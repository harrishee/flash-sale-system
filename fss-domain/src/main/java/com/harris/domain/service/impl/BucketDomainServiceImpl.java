package com.harris.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.domain.exception.DomainErrorCode;
import com.harris.domain.model.enums.BucketEventType;
import com.harris.domain.model.event.BucketEvent;
import com.harris.domain.event.DomainEventPublisher;
import com.harris.domain.exception.DomainException;
import com.harris.domain.model.Bucket;
import com.harris.domain.repository.BucketRepository;
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
@ConditionalOnProperty(name = "place_order_type", havingValue = "bucket", matchIfMissing = true)
public class BucketDomainServiceImpl implements BucketDomainService {
    @Resource
    private BucketRepository bucketRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public List<Bucket> getBucketsByItemId(Long itemId) {
        // Validate params
        if (itemId == null || itemId <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }
        return bucketRepository.findBucketsByItemId(itemId);
    }

    @Override
    public boolean arrangeBucketsByItemId(Long itemId, List<Bucket> buckets) {
        log.info("arrangeBuckets: {},{}", itemId, JSON.toJSONString(buckets));

        // Validate params
        if (itemId == null || itemId <= 0 || CollectionUtils.isEmpty(buckets)) {
            log.error("arrangeBuckets, invalid params: {}", itemId);
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Ensure that there is one and only one primary bucket
        Optional<Bucket> primaryBuckets = buckets.stream().filter(Bucket::isPrimary).findFirst();
        if (!primaryBuckets.isPresent()) {
            throw new DomainException(DomainErrorCode.PRIMARY_BUCKET_IS_MISSING);
        }
        if (buckets.stream().filter(Bucket::isPrimary).count() > 1) {
            throw new DomainException(DomainErrorCode.MULTI_PRIMARY_BUCKETS_FOUND_BUT_EXPECT_ONE);
        }

        // Validate each bucket in the list
        buckets.forEach(stockBucket -> {
            // Validate total stock amount
            if (stockBucket.getTotalStock() == null || stockBucket.getTotalStock() < 0) {
                throw new DomainException(DomainErrorCode.TOTAL_STOCK_AMOUNT_INVALID);
            }

            // Validate available stock amount
            if (stockBucket.getAvailableStock() == null || stockBucket.getAvailableStock() <= 0) {
                throw new DomainException(DomainErrorCode.AVAILABLE_STOCK_AMOUNT_INVALID);
            }

            // Ensure available stock amount equals total stock amount for non-primary buckets
            if (!stockBucket.getAvailableStock()
                    .equals(stockBucket.getTotalStock()) && !stockBucket.isPrimary()) {
                throw new DomainException(DomainErrorCode.AVAILABLE_AMOUNT_NOT_EQUALS_TO_TOTAL_AMOUNT);
            }

            // Validate that the bucket belongs to the given item
            if (!itemId.equals(stockBucket.getItemId())) {
                throw new DomainException(DomainErrorCode.STOCK_BUCKET_ITEM_INVALID);
            }
        });

        // Save buckets and handle the result
        boolean saveSuccess = bucketRepository.saveBuckets(itemId, buckets);
        if (!saveSuccess) {
            return false;
        }
        log.info("arrangeBuckets, buckets arranged: {}", itemId);

        // Publish bucket event
        BucketEvent bucketEvent = new BucketEvent();
        bucketEvent.setBucketEventType(BucketEventType.ARRANGED);
        bucketEvent.setItemId(itemId);
        domainEventPublisher.publish(bucketEvent);
        log.info("arrangeBuckets, buckets arrange event published: {}", itemId);
        return true;
    }

    @Override
    public boolean suspendBuckets(Long itemId) {
        log.info("suspendBuckets: {}", itemId);

        // Validate params
        if (itemId == null || itemId <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Suspend buckets and handle the result
        boolean suspendSuccess = bucketRepository.suspendBucketsByItemId(itemId);
        if (!suspendSuccess) {
            return false;
        }
        log.info("suspendBuckets, buckets suspended: {}", itemId);

        // Publish bucket event
        BucketEvent bucketEvent = new BucketEvent();
        bucketEvent.setBucketEventType(BucketEventType.DISABLED);
        bucketEvent.setItemId(itemId);
        domainEventPublisher.publish(bucketEvent);
        log.info("suspendBuckets, buckets suspend event published: {}", itemId);
        return true;
    }

    @Override
    public boolean resumeBuckets(Long itemId) {
        log.info("resumeBuckets: {}", itemId);

        // Validate params
        if (itemId == null || itemId <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_PARAMS);
        }

        // Resume buckets and handle the result
        boolean success = bucketRepository.resumeBucketsByItemId(itemId);
        if (!success) {
            return false;
        }
        log.info("resumeBuckets, buckets resumed: {}", itemId);

        // Publish bucket event
        BucketEvent bucketEvent = new BucketEvent();
        bucketEvent.setBucketEventType(BucketEventType.ENABLED);
        bucketEvent.setItemId(itemId);
        domainEventPublisher.publish(bucketEvent);
        log.info("resumeBuckets, buckets resume event published: {}", itemId);
        return true;
    }
}
