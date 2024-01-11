package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.Bucket;
import com.harris.infra.persistence.model.BucketDO;
import org.springframework.beans.BeanUtils;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public class BucketConverter {
    private BucketConverter() {
    }

    public static BucketDO toDataObject(Bucket bucket) {
        BucketDO bucketDO = new BucketDO();
        BeanUtils.copyProperties(bucket, bucketDO);
        return bucketDO;
    }

    public static Bucket toDomainObject(BucketDO bucketDO) {
        Bucket bucket = new Bucket();
        BeanUtils.copyProperties(bucketDO, bucket);
        return bucket;
    }
}
