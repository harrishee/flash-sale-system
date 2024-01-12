package com.harris.infra.persistence.mapper;

import com.harris.infra.persistence.model.BucketDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BucketMapper {
    boolean increaseStockByItemId(@Param("itemId") Long itemId, @Param("quantity") Integer quantity, @Param("serialNo") Integer serialNo);

    boolean decreaseStockByItemId(@Param("itemId") Long itemId, @Param("quantity") Integer quantity, @Param("serialNo") Integer serialNo);

    List<BucketDO> getBucketsByItemId(@Param("itemId") Long itemId);

    int updateStatusByItemId(@Param("itemId") Long itemId, @Param("status") int status);

    void deleteByItemId(@Param("itemId") Long itemId);

    void insertBatch(List<BucketDO> bucketDOS);
}
