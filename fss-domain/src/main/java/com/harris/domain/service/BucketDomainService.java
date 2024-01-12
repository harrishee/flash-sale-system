package com.harris.domain.service;

import com.harris.domain.model.Bucket;

import java.util.List;

public interface BucketDomainService {
    List<Bucket> getBucketsByItemId(Long itemId);

    boolean arrangeBucketsByItemId(Long itemId, List<Bucket> buckets);

    boolean suspendBuckets(Long itemId);

    boolean resumeBuckets(Long itemId);
}
