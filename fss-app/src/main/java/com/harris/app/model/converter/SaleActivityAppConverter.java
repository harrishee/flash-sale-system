package com.harris.app.model.converter;

import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleActivityAppConverter {
    public static SaleActivity toDomainModel(PublishActivityCommand publishActivityCommand) {
        if (publishActivityCommand == null) {
            return null;
        }

        SaleActivity saleActivity = new SaleActivity();
        BeanUtils.copyProperties(publishActivityCommand, saleActivity);
        return saleActivity;
    }

    public static PageQuery toPageQuery(SaleActivitiesQuery saleActivitiesQuery) {
        if (saleActivitiesQuery == null) {
            return new PageQuery();
        }

        PageQuery pageQuery = new PageQuery();
        BeanUtils.copyProperties(saleActivitiesQuery, pageQuery);
        return pageQuery;
    }

    public static SaleActivityDTO toDTO(SaleActivity saleActivity) {
        if (saleActivity == null) {
            return null;
        }

        SaleActivityDTO saleActivityDTO = new SaleActivityDTO();
        BeanUtils.copyProperties(saleActivity, saleActivityDTO);
        return saleActivityDTO;
    }
}
