package com.harris.domain.service;

import com.harris.domain.model.Bucket;

import java.util.List;

public interface BucketDomainService {
    /**
     * Retrieves buckets by item ID.
     *
     * @param itemId the ID of the item
     * @return a list of buckets associated with the item
     */
    List<Bucket> getBucketsByItemId(Long itemId);

    /**
     * Arranges buckets for a given item ID.
     *
     * @param itemId  the ID of the item
     * @param buckets the list of buckets to arrange
     * @return arrange result
     */
    boolean arrangeBucketsByItemId(Long itemId, List<Bucket> buckets);

    /**
     * Suspends the buckets for a given item ID.
     *
     * @param itemId the ID of the item
     * @return suspend result
     */
    boolean suspendBuckets(Long itemId);

    /**
     * Resumes the buckets for a given item ID.
     *
     * @param itemId the ID of the item
     * @return resume result
     */
    boolean resumeBuckets(Long itemId);
}
