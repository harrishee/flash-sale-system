package com.harris.domain.repository.impl;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;
import com.harris.domain.repository.FlashOrderRepository;
import com.harris.infra.persistence.converter.FlashOrderConverter;
import com.harris.infra.persistence.mapper.FlashOrderMapper;
import com.harris.infra.persistence.model.FlashOrderDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlashOrderRepositoryImpl implements FlashOrderRepository {
    @Resource
    private FlashOrderMapper flashOrderMapper;

    @Override
    public boolean saveOrder(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = FlashOrderConverter.toDataObject(flashOrder);
        int effectedRows = flashOrderMapper.insertOrder(flashOrderDO);
        return effectedRows == 1;
    }

    @Override
    public boolean updateStatusForOrder(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = FlashOrderConverter.toDataObject(flashOrder);
        int effectedRows = flashOrderMapper.updateStatus(flashOrderDO);
        return effectedRows == 1;
    }

    @Override
    public Optional<FlashOrder> findOrderById(Long orderId) {
        FlashOrderDO flashOrderDO = flashOrderMapper.getOrderById(orderId);
        if (flashOrderDO == null) {
            return Optional.empty();
        }
        FlashOrder flashOrder = FlashOrderConverter.toDomainObject(flashOrderDO);
        return Optional.of(flashOrder);
    }

    @Override
    public List<FlashOrder> findOrdersByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashOrderMapper.getOrdersByCondition(pagesQueryCondition)
                .stream()
                .map(FlashOrderConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public int countOrdersByCondition(PagesQueryCondition buildParams) {
        return flashOrderMapper.countOrdersByCondition();
    }
}
