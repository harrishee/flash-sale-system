package com.harris.infra.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.repository.SaleOrderRepository;
import com.harris.infra.model.converter.SaleOrderConverter;
import com.harris.infra.mapper.SaleOrderMapper;
import com.harris.infra.model.SaleOrderDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SaleOrderRepositoryImpl implements SaleOrderRepository {
    @Resource
    private SaleOrderMapper saleOrderMapper;

    @Override
    public Optional<SaleOrder> findOrderById(Long orderId) {
        // Get the DO from the mapper and validate
        SaleOrderDO saleOrderDO = saleOrderMapper.getOrderById(orderId);
        if (saleOrderDO == null) {
            return Optional.empty();
        }

        // Convert the DO to a domain model
        SaleOrder saleOrder = SaleOrderConverter.toDomainModel(saleOrderDO);
        return Optional.of(saleOrder);
    }

    @Override
    public List<SaleOrder> findOrdersByCondition(PageQuery pageQuery) {
        // Get the DOs from the mapper and convert to domain models
        return saleOrderMapper.getOrdersByCondition(pageQuery)
                .stream()
                .map(SaleOrderConverter::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public int countOrdersByCondition(PageQuery pageQuery) {
        return saleOrderMapper.countOrdersByCondition(pageQuery);
    }

    @Override
    public boolean saveOrder(SaleOrder saleOrder) {
        // Convert the domain model to a DO
        SaleOrderDO saleOrderDO = SaleOrderConverter.toDO(saleOrder);

        // Should be exactly 1 row affected
        int effectedRows = saleOrderMapper.insertOrder(saleOrderDO);
        return effectedRows == 1;
    }

    @Override
    public boolean updateStatus(SaleOrder saleOrder) {
        // Convert the domain model to a DO
        SaleOrderDO saleOrderDO = SaleOrderConverter.toDO(saleOrder);

        // Should be exactly 1 row affected
        int effectedRows = saleOrderMapper.updateStatus(saleOrderDO);
        return effectedRows == 1;
    }
}
