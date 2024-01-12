package com.harris.domain.repository.impl;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;
import com.harris.domain.repository.FssOrderRepository;
import com.harris.infra.persistence.converter.FssOrderConverter;
import com.harris.infra.persistence.mapper.FssOrderMapper;
import com.harris.infra.persistence.model.FlashOrderDO;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FssOrderRepositoryImpl implements FssOrderRepository {
    @Resource
    private FssOrderMapper fssOrderMapper;

    @Override
    public boolean saveOrder(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = FssOrderConverter.toDataObject(flashOrder);
        int effectedRows = fssOrderMapper.insertOrder(flashOrderDO);
        return effectedRows == 1;
    }

    @Override
    public boolean updateStatusForOrder(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = FssOrderConverter.toDataObject(flashOrder);
        int effectedRows = fssOrderMapper.updateStatus(flashOrderDO);
        return effectedRows == 1;
    }

    @Override
    public Optional<FlashOrder> findOrderById(Long orderId) {
        FlashOrderDO flashOrderDO = fssOrderMapper.getOrderById(orderId);
        if (flashOrderDO == null) {
            return Optional.empty();
        }
        FlashOrder flashOrder = FssOrderConverter.toDomainObject(flashOrderDO);
        return Optional.of(flashOrder);
    }

    @Override
    public List<FlashOrder> findOrdersByCondition(PagesQueryCondition pagesQueryCondition) {
        return fssOrderMapper.getOrdersByCondition(pagesQueryCondition)
                .stream()
                .map(FssOrderConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public int countOrdersByCondition(PagesQueryCondition buildParams) {
        return fssOrderMapper.countOrdersByCondition();
    }
}
