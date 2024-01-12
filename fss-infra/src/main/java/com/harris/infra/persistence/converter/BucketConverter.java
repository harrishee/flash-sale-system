package com.harris.infra.persistence.converter;

import com.harris.domain.model.Bucket;
import com.harris.infra.persistence.model.BucketDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BucketConverter {

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
