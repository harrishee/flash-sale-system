package com.harris.app.service;

import com.harris.app.model.command.BucketArrangeCommand;
import com.harris.app.model.dto.TotalStockBucketDTO;
import com.harris.app.model.result.AppSingleResult;

public interface BucketAppService {
    AppSingleResult<TotalStockBucketDTO> getTotalStockBucket(Long userId, Long itemId);

    <T> AppSingleResult<T> arrangeStockBucket(Long userId, Long itemId, BucketArrangeCommand arrangementCommand);
}
