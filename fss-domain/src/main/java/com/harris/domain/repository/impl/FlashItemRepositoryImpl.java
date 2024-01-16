package com.harris.domain.repository.impl;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.repository.FlashItemRepository;
import com.harris.infra.persistence.converter.FlashItemConverter;
import com.harris.infra.persistence.mapper.FlashItemMapper;
import com.harris.infra.persistence.model.FlashItemDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlashItemRepositoryImpl implements FlashItemRepository {
    @Resource
    private FlashItemMapper flashItemMapper;

    @Override
    public int saveItem(FlashItem flashItem) {
        FlashItemDO flashItemDO = FlashItemConverter.toDO(flashItem);
        if (flashItem.getId() == null) {
            return flashItemMapper.insertItem(flashItemDO);
        }
        return flashItemMapper.updateItem(flashItemDO);
    }

    @Override
    public Optional<FlashItem> findItemById(Long itemId) {
        FlashItemDO flashItemDO = flashItemMapper.getItemById(itemId);
        if (flashItemDO == null) {
            return Optional.empty();
        }
        FlashItem flashItem = FlashItemConverter.toDomainObj(flashItemDO);
        return Optional.of(flashItem);
    }

    @Override
    public List<FlashItem> findItemsByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashItemMapper.getItemsByCondition(pagesQueryCondition)
                .stream()
                .map(FlashItemConverter::toDomainObj)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countItemsByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashItemMapper.countItemsByCondition(pagesQueryCondition);
    }

    @Override
    public boolean decreaseStockForItem(Long itemId, Integer quantity) {
        return flashItemMapper.decreaseStockById(itemId, quantity) == 1;
    }

    @Override
    public boolean increaseStockForItem(Long itemId, Integer quantity) {
        return flashItemMapper.increaseStockById(itemId, quantity) == 1;
    }
}
