package com.harris.infra.mapper;

import com.harris.infra.model.BucketDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BucketMapper {
    List<BucketDO> getBucketsByItemId(@Param("itemId") Long itemId);

    void insertBuckets(List<BucketDO> bucketDOS);

    int updateBucketStatusByItemId(@Param("itemId") Long itemId, @Param("status") int status);

    void deleteBucketByItemId(@Param("itemId") Long itemId);

    boolean reduceStockByItemId(@Param("itemId") Long itemId,
                                @Param("quantity") Integer quantity,
                                @Param("serialNo") Integer serialNo);

    boolean addStockByItemId(@Param("itemId") Long itemId,
                             @Param("quantity") Integer quantity,
                             @Param("serialNo") Integer serialNo);
}
