package com.harris.infra.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.repository.FlashOrderRepository;
import com.harris.infra.model.converter.SaleOrderToDOConverter;
import com.harris.infra.mapper.SaleOrderMapper;
import com.harris.infra.model.SaleOrderDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SaleOrderRepositoryImpl implements FlashOrderRepository {
    @Resource
    private SaleOrderMapper saleOrderMapper;

    @Override
    public Optional<SaleOrder> findOrderById(Long orderId) {
        SaleOrderDO saleOrderDO = saleOrderMapper.getOrderById(orderId);
        if (saleOrderDO == null) {
            return Optional.empty();
        }
        SaleOrder saleOrder = SaleOrderToDOConverter.toDomainModel(saleOrderDO);
        return Optional.of(saleOrder);
    }

    @Override
    public List<SaleOrder> findOrdersByCondition(PageQueryCondition pageQueryCondition) {
        return saleOrderMapper.getOrdersByCondition(pageQueryCondition)
                .stream()
                .map(SaleOrderToDOConverter::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public int countOrdersByCondition(PageQueryCondition buildParams) {
        return saleOrderMapper.countOrdersByCondition(buildParams);
    }

    @Override
    public boolean saveOrder(SaleOrder saleOrder) {
        SaleOrderDO saleOrderDO = SaleOrderToDOConverter.toDO(saleOrder);
        int effectedRows = saleOrderMapper.insertOrder(saleOrderDO);
        return effectedRows == 1;
    }

    @Override
    public boolean updateStatusForOrder(SaleOrder saleOrder) {
        SaleOrderDO saleOrderDO = SaleOrderToDOConverter.toDO(saleOrder);
        int effectedRows = saleOrderMapper.updateStatus(saleOrderDO);
        return effectedRows == 1;
    }
}
