package com.harris.app.service;

import com.harris.app.model.dto.TotalStockBucketDTO;

public interface BucketArrangeService {
    TotalStockBucketDTO queryTotalStockBucket(Long itemId);

    void arrangeStockBucket(Long itemId, Integer stocksAmount, Integer bucketsAmount, Integer assignmentMode);
}
