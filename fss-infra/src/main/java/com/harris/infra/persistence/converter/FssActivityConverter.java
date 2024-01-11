package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.FssActivity;
import com.harris.infra.persistence.model.FssActivityDO;
import org.springframework.beans.BeanUtils;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public class FssActivityConverter {
    private FssActivityConverter() {
    }

    public static FssActivityDO toDataObject(FssActivity fssActivity) {
        FssActivityDO fssActivityDO = new FssActivityDO();
        BeanUtils.copyProperties(fssActivity, fssActivityDO);
        return fssActivityDO;
    }

    public static FssActivity toDomainObject(FssActivityDO fssActivityDO) {
        FssActivity fssActivity = new FssActivity();
        BeanUtils.copyProperties(fssActivityDO, fssActivity);
        return fssActivity;
    }
}
