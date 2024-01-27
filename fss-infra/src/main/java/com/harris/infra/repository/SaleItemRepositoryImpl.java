package com.harris.infra.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.repository.SaleItemRepository;
import com.harris.infra.model.SaleItemDO;
import com.harris.infra.model.converter.SaleItemConverter;
import com.harris.infra.mapper.SaleItemMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SaleItemRepositoryImpl implements SaleItemRepository {
    @Resource
    private SaleItemMapper saleItemMapper;

    @Override
    public Optional<SaleItem> findItemById(Long itemId) {
        // Get the DO from the mapper and validate
        SaleItemDO saleItemDO = saleItemMapper.getItemById(itemId);
        if (saleItemDO == null) {
            return Optional.empty();
        }

        // Convert the DO to a domain model
        SaleItem saleItem = SaleItemConverter.toDomainModel(saleItemDO);
        return Optional.of(saleItem);
    }

    @Override
    public List<SaleItem> findItemsByCondition(PageQuery pageQuery) {
        // Get the DOs from the mapper and convert to domain models
        return saleItemMapper.getItemsByCondition(pageQuery)
                .stream()
                .map(SaleItemConverter::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countItemsByCondition(PageQuery pageQuery) {
        return saleItemMapper.countItemsByCondition(pageQuery);
    }

    @Override
    public int saveItem(SaleItem saleItem) {
        // Convert the domain model to a DO
        SaleItemDO saleItemDO = SaleItemConverter.toDO(saleItem);

        // If the ID is null, insert the new item
        if (saleItem.getId() == null) {
            return saleItemMapper.insertItem(saleItemDO);
        }

        // Otherwise, update the existed item
        return saleItemMapper.updateItem(saleItemDO);
    }

    @Override
    public boolean deductStockForItem(Long itemId, Integer quantity) {
        // Should be exactly 1 row affected
        return saleItemMapper.reduceStockById(itemId, quantity) == 1;
    }

    @Override
    public boolean revertStockForItem(Long itemId, Integer quantity) {
        // Should be exactly 1 row affected
        return saleItemMapper.addStockById(itemId, quantity) == 1;
    }
}
