package com.harris.infra.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.domain.repository.SaleOrderRepository;
import com.harris.infra.mapper.SaleOrderMapper;
import com.harris.infra.model.SaleOrderDO;
import com.harris.infra.util.InfraConverter;
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
        // 从 mapper 中获取 DO
        SaleOrderDO saleOrderDO = saleOrderMapper.getOrderById(orderId);
        if (saleOrderDO == null) return Optional.empty();
        
        // 将 DO 转换为 domain model
        SaleOrder saleOrder = InfraConverter.toSaleOrderDomain(saleOrderDO);
        return Optional.of(saleOrder);
    }
    
    @Override
    public List<SaleOrder> findAllOrderByCondition(PageQuery pageQuery) {
        // 从 mapper 中获取 DO 列表，然后转换为 domain model 列表
        return saleOrderMapper.getOrdersByCondition(pageQuery)
                .stream()
                .map(InfraConverter::toSaleOrderDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public int countAllOrderByCondition(PageQuery pageQuery) {
        return saleOrderMapper.countOrdersByCondition(pageQuery);
    }
    
    @Override
    public boolean saveOrder(SaleOrder saleOrder) {
        // 将 domain model 转换为 DO
        SaleOrderDO saleOrderDO = InfraConverter.toSaleOrderDO(saleOrder);
        
        // 插入新的订单，并检查是否插入成功
        int effectedRows = saleOrderMapper.insertOrder(saleOrderDO);
        return effectedRows == 1;
    }
    
    @Override
    public boolean updateStatus(SaleOrder saleOrder) {
        // 将 domain model 转换为 DO
        SaleOrderDO saleOrderDO = InfraConverter.toSaleOrderDO(saleOrder);
        
        // 更新订单状态，并检查是否更新成功
        return saleOrderMapper.updateStatus(saleOrderDO) == 1;
    }
}
