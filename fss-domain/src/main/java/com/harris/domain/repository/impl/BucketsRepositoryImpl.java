package com.harris.domain.repository.impl;

import com.harris.domain.model.Bucket;
import com.harris.domain.repository.BucketsRepository;
import com.harris.infra.persistence.converter.BucketConverter;
import com.harris.infra.persistence.mapper.BucketMapper;
import com.harris.infra.persistence.model.BucketDO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class BucketsRepositoryImpl implements BucketsRepository {
    @Resource
    private BucketMapper bucketMapper;

    @Override
    public boolean saveBuckets(Long itemId, List<Bucket> buckets) {
        if (itemId == null || CollectionUtils.isEmpty(buckets)) {
            return false;
        }
        List<BucketDO> bucketDOS = buckets.stream().map(BucketConverter::toDO).collect(Collectors.toList());
        bucketMapper.deleteByItemId(itemId);
        bucketMapper.insertBatch(bucketDOS);
        return true;
    }

    @Override
    public boolean decreaseStockForItem(Long itemId, Integer quantity, Integer serialNo) {
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }
        return bucketMapper.decreaseStockByItemId(itemId, quantity, serialNo);
    }

    @Override
    public boolean increaseStockForItem(Long itemId, Integer quantity, Integer serialNo) {
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }
        return bucketMapper.increaseStockByItemId(itemId, quantity, serialNo);
    }

    @Override
    public List<Bucket> findBucketsByItemId(Long itemId) {
        if (itemId == null) {
            return new ArrayList<>();
        }
        List<BucketDO> bucketDOS = bucketMapper.getBucketsByItemId(itemId);
        return bucketDOS.stream().map(BucketConverter::toDomainObj).collect(toList());
    }

    @Override
    public boolean suspendBucketsByItemId(Long itemId) {
        if (itemId == null) {
            return false;
        }
        bucketMapper.updateStatusByItemId(itemId, 0);
        return true;
    }

    @Override
    public boolean resumeBucketsByItemId(Long itemId) {
        if (itemId == null) {
            return false;
        }
        bucketMapper.updateStatusByItemId(itemId, 1);
        return true;
    }
}
