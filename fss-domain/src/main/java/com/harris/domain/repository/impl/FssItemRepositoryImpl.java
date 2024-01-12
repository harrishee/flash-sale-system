package com.harris.domain.repository.impl;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.repository.FssItemRepository;
import com.harris.infra.persistence.converter.FssItemConverter;
import com.harris.infra.persistence.mapper.FssItemMapper;
import com.harris.infra.persistence.model.FlashItemDO;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FssItemRepositoryImpl implements FssItemRepository {
    @Resource
    private FssItemMapper fssItemMapper;

    @Override
    public int saveItem(FlashItem flashItem) {
        FlashItemDO flashItemDO = FssItemConverter.toDataObject(flashItem);
        if (flashItem.getId() == null) {
            return fssItemMapper.insertItem(flashItemDO);
        }
        return fssItemMapper.updateItem(flashItemDO);
    }

    @Override
    public Optional<FlashItem> findItemById(Long itemId) {
        FlashItemDO flashItemDO = fssItemMapper.getItemById(itemId);
        if (flashItemDO == null) {
            return Optional.empty();
        }
        FlashItem flashItem = FssItemConverter.toDomainObject(flashItemDO);
        return Optional.of(flashItem);
    }

    @Override
    public List<FlashItem> findItemsByCondition(PagesQueryCondition pagesQueryCondition) {
        return fssItemMapper.getItemsByCondition(pagesQueryCondition)
                .stream()
                .map(FssItemConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countItemsByCondition(PagesQueryCondition pagesQueryCondition) {
        return fssItemMapper.countItemsByCondition(pagesQueryCondition);
    }

    @Override
    public boolean decreaseStockForItem(Long itemId, Integer quantity) {
        return fssItemMapper.decreaseStockById(itemId, quantity) == 1;
    }

    @Override
    public boolean increaseStockForItem(Long itemId, Integer quantity) {
        return fssItemMapper.increaseStockById(itemId, quantity) == 1;
    }
}
