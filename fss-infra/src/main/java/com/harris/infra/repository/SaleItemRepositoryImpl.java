package com.harris.infra.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.repository.FlashItemRepository;
import com.harris.infra.model.SaleItemDO;
import com.harris.infra.model.converter.SaleItemToDOConverter;
import com.harris.infra.mapper.SaleItemMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SaleItemRepositoryImpl implements FlashItemRepository {
    @Resource
    private SaleItemMapper saleItemMapper;

    @Override
    public Optional<SaleItem> findItemById(Long itemId) {
        SaleItemDO saleItemDO = saleItemMapper.getItemById(itemId);
        if (saleItemDO == null) {
            return Optional.empty();
        }
        SaleItem saleItem = SaleItemToDOConverter.toDomainModel(saleItemDO);
        return Optional.of(saleItem);
    }

    @Override
    public List<SaleItem> findItemsByCondition(PageQueryCondition pageQueryCondition) {
        return saleItemMapper.getItemsByCondition(pageQueryCondition)
                .stream()
                .map(SaleItemToDOConverter::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countItemsByCondition(PageQueryCondition pageQueryCondition) {
        return saleItemMapper.countItemsByCondition(pageQueryCondition);
    }

    @Override
    public int saveItem(SaleItem saleItem) {
        SaleItemDO saleItemDO = SaleItemToDOConverter.toDO(saleItem);
        if (saleItem.getId() == null) {
            return saleItemMapper.insertItem(saleItemDO);
        }
        return saleItemMapper.updateItem(saleItemDO);
    }

    @Override
    public boolean deductStockForItem(Long itemId, Integer quantity) {
        return saleItemMapper.reduceStockById(itemId, quantity) == 1;
    }

    @Override
    public boolean revertStockForItem(Long itemId, Integer quantity) {
        return saleItemMapper.addStockById(itemId, quantity) == 1;
    }
}
