package com.harris.app.model.converter;

import com.harris.app.model.dto.BucketDTO;
import com.harris.domain.model.Bucket;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockBucketConverter {
    public static BucketDTO toDTO(Bucket bucket) {
        if (bucket == null) {
            return null;
        }

        BucketDTO bucketDTO = new BucketDTO();
        BeanUtils.copyProperties(bucket, bucketDTO);
        return bucketDTO;
    }
}
