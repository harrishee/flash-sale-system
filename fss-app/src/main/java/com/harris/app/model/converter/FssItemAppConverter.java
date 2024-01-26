package com.harris.app.model.converter;

import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FssItemAppConverter {
    public static SaleItem toDomainModel(PublishItemCommand publishItemCommand) {
        SaleItem saleItem = new SaleItem();
        BeanUtils.copyProperties(publishItemCommand, saleItem);
        return saleItem;
    }

    public static PageQueryCondition toQuery(SaleItemsQuery saleItemsQuery) {
        PageQueryCondition pageQueryCondition = new PageQueryCondition();
        BeanUtils.copyProperties(saleItemsQuery, pageQueryCondition);
        return pageQueryCondition;
    }

    public static SaleItemDTO toDTO(SaleItem saleItem) {
        SaleItemDTO saleItemDto = new SaleItemDTO();
        BeanUtils.copyProperties(saleItem, saleItemDto);
        return saleItemDto;
    }
}
