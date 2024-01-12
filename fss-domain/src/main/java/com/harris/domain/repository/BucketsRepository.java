package com.harris.domain.repository;

import com.harris.domain.model.entity.Bucket;

import java.util.List;

public interface BucketsRepository {
    boolean saveBuckets(Long itemId, List<Bucket> buckets);

    boolean decreaseStockForItem(Long itemId, Integer quantity, Integer serialNo);

    boolean increaseStockForItem(Long itemId, Integer quantity, Integer serialNo);

    List<Bucket> findBucketsByItemId(Long itemId);

    boolean suspendBucketsByItemId(Long itemId);

    boolean resumeBucketsByItemId(Long itemId);
}
