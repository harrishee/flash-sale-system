package com.harris.infra.repository;

import com.harris.domain.model.Bucket;
import com.harris.domain.repository.BucketsRepository;
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
public class BucketRepositoryImpl implements BucketsRepository {
    @Resource
    private BucketMapper bucketMapper;

    @Override
    public List<Bucket> findBucketsByItemId(Long itemId) {
        if (itemId == null) {
            return new ArrayList<>();
        }
        List<BucketDO> bucketDOS = bucketMapper.getBucketsByItemId(itemId);
        return bucketDOS.stream().map(BucketToDOConverter::toDomainModel).collect(toList());
    }

    @Override
    public boolean saveBuckets(Long itemId, List<Bucket> buckets) {
        if (itemId == null || CollectionUtils.isEmpty(buckets)) {
            return false;
        }
        List<BucketDO> bucketDOS = buckets.stream().map(BucketToDOConverter::toDO).collect(Collectors.toList());
        bucketMapper.deleteBucketByItemId(itemId);
        bucketMapper.insertBuckets(bucketDOS);
        return true;
    }

    @Override
    public boolean suspendBucketsByItemId(Long itemId) {
        if (itemId == null) {
            return false;
        }
        bucketMapper.updateBucketStatusByItemId(itemId, 0);
        return true;
    }

    @Override
    public boolean resumeBucketsByItemId(Long itemId) {
        if (itemId == null) {
            return false;
        }
        bucketMapper.updateBucketStatusByItemId(itemId, 1);
        return true;
    }

    @Override
    public boolean deductStockForItem(Long itemId, Integer quantity, Integer serialNo) {
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }
        return bucketMapper.reduceStockByItemId(itemId, quantity, serialNo);
    }

    @Override
    public boolean revertStockForItem(Long itemId, Integer quantity, Integer serialNo) {
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }
        return bucketMapper.addStockByItemId(itemId, quantity, serialNo);
    }
}
