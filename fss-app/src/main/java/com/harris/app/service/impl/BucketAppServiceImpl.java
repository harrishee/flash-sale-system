package com.harris.app.service.impl;

import com.harris.app.model.command.BucketArrangeCommand;
import com.harris.app.model.dto.TotalStockBucketDTO;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.BucketAppService;

public class BucketAppServiceImpl implements BucketAppService {
    @Override
    public AppSingleResult<TotalStockBucketDTO> getTotalStockBucket(Long userId, Long itemId) {
        return null;
    }

    @Override
    public <T> AppSingleResult<T> arrangeStockBucket(Long userId, Long itemId, BucketArrangeCommand arrangementCommand) {
        return null;
    }
}
