package com.harris.domain.repository;

import com.harris.domain.model.Bucket;

import java.util.List;

public interface BucketsRepository {
    List<Bucket> findBucketsByItemId(Long itemId);

    boolean saveBuckets(Long itemId, List<Bucket> buckets);

    boolean suspendBucketsByItemId(Long itemId);

    boolean resumeBucketsByItemId(Long itemId);

    boolean deductStockForItem(Long itemId, Integer quantity, Integer serialNo);

    boolean revertStockForItem(Long itemId, Integer quantity, Integer serialNo);
}
