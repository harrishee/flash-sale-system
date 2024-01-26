package com.harris.infra.model.converter;

import com.harris.domain.model.Bucket;
import com.harris.infra.model.BucketDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BucketToDOConverter {
    public static BucketDO toDO(Bucket bucket) {
        BucketDO bucketDO = new BucketDO();
        BeanUtils.copyProperties(bucket, bucketDO);
        return bucketDO;
    }

    public static Bucket toDomainModel(BucketDO bucketDO) {
        Bucket bucket = new Bucket();
        BeanUtils.copyProperties(bucketDO, bucket);
        return bucket;
    }
}
