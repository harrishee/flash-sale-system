package com.harris.app.model.converter;

import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleItemAppConverter {
    public static SaleItem toDomainModel(PublishItemCommand publishItemCommand) {
        if (publishItemCommand == null) {
            return null;
        }

        SaleItem saleItem = new SaleItem();
        BeanUtils.copyProperties(publishItemCommand, saleItem);
        return saleItem;
    }

    public static PageQuery toPageQuery(SaleItemsQuery saleItemsQuery) {
        if (saleItemsQuery == null) {
            return new PageQuery();
        }

        PageQuery pageQuery = new PageQuery();
        BeanUtils.copyProperties(saleItemsQuery, pageQuery);
        return pageQuery;
    }

    public static SaleItemDTO toDTO(SaleItem saleItem) {
        if (saleItem == null) {
            return null;
        }

        SaleItemDTO saleItemDto = new SaleItemDTO();
        BeanUtils.copyProperties(saleItem, saleItemDto);
        return saleItemDto;
    }
}
