package com.harris.infra.model.converter;

import com.harris.domain.model.entity.SaleActivity;
import com.harris.infra.model.SaleActivityDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleActivityToDOConverter {
    public static SaleActivityDO toDO(SaleActivity saleActivity) {
        SaleActivityDO saleActivityDO = new SaleActivityDO();
        BeanUtils.copyProperties(saleActivity, saleActivityDO);
        return saleActivityDO;
    }

    public static SaleActivity toDomainModel(SaleActivityDO saleActivityDO) {
        SaleActivity saleActivity = new SaleActivity();
        BeanUtils.copyProperties(saleActivityDO, saleActivity);
        return saleActivity;
    }
}
