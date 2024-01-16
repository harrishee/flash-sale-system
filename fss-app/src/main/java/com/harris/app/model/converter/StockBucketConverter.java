package com.harris.app.model.converter;

import com.harris.app.model.dto.StockBucketDTO;
import com.harris.domain.model.Bucket;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockBucketConverter {
    public static StockBucketDTO toDTO(Bucket bucket) {
        if (bucket == null) {
            return null;
        }
        StockBucketDTO stockBucketDTO = new StockBucketDTO();
        BeanUtils.copyProperties(bucket, stockBucketDTO);
        return stockBucketDTO;
    }
}
