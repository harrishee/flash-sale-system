package com.harris.infra.repository;

import com.harris.domain.model.Bucket;
import com.harris.domain.repository.BucketRepository;
import com.harris.infra.mapper.BucketMapper;
import com.harris.infra.model.BucketDO;
import com.harris.infra.model.converter.BucketToDOConverter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class BucketRepositoryImpl implements BucketRepository {
    @Resource
    private BucketMapper bucketMapper;

    @Override
    public List<Bucket> findBucketsByItemId(Long itemId) {
        // Validate params
        if (itemId == null) {
            return new ArrayList<>();
        }

        // Get bucket DOs from the mapper and convert to domain models
        List<BucketDO> bucketDOS = bucketMapper.getBucketsByItemId(itemId);
        return bucketDOS.stream().map(BucketToDOConverter::toDomainModel).collect(toList());
    }

    @Override
    public boolean saveBuckets(Long itemId, List<Bucket> buckets) {
        // Validate params
        if (itemId == null || CollectionUtils.isEmpty(buckets)) {
            return false;
        }

        // Convert to DOs
        List<BucketDO> bucketDOS = buckets.stream().map(BucketToDOConverter::toDO).collect(Collectors.toList());
        // Remove existing buckets for this item
        bucketMapper.deleteBucketByItemId(itemId);
        // Insert new buckets
        bucketMapper.insertBuckets(bucketDOS);
        return true;
    }

    @Override
    public boolean suspendBucketsByItemId(Long itemId) {
        // Validate params
        if (itemId == null) {
            return false;
        }

        // Update the status of all buckets for this item to 0
        bucketMapper.updateBucketStatusByItemId(itemId, 0);
        return true;
    }

    @Override
    public boolean resumeBucketsByItemId(Long itemId) {
        // Validate params
        if (itemId == null) {
            return false;
        }

        // Update the status of all buckets for this item to 1
        bucketMapper.updateBucketStatusByItemId(itemId, 1);
        return true;
    }

    @Override
    public boolean deductStockForItem(Long itemId, Integer quantity, Integer serialNo) {
        // Validate params
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }

        // Reduce the stock for this item by the given quantity
        return bucketMapper.reduceStockByItemId(itemId, quantity, serialNo);
    }

    @Override
    public boolean revertStockForItem(Long itemId, Integer quantity, Integer serialNo) {
        // Validate params
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }

        // Increase the stock for this item by the given quantity
        return bucketMapper.addStockByItemId(itemId, quantity, serialNo);
    }
}
