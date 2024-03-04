package com.harris.infra.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.repository.SaleItemRepository;
import com.harris.infra.mapper.SaleItemMapper;
import com.harris.infra.model.SaleItemDO;
import com.harris.infra.util.InfraConverter;
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
        // 从 mapper 中获取 DO
        SaleItemDO saleItemDO = saleItemMapper.getItemById(itemId);
        if (saleItemDO == null) return Optional.empty();
        
        // 将 DO 转换为 domain model
        SaleItem saleItem = InfraConverter.toSaleItemDomain(saleItemDO);
        return Optional.of(saleItem);
    }
    
    @Override
    public List<SaleItem> findAllItemByCondition(PageQuery pageQuery) {
        // 从 mapper 中获取 DO 列表，然后转换为 domain model 列表
        return saleItemMapper.getItemsByCondition(pageQuery)
                .stream()
                .map(InfraConverter::toSaleItemDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer countAllItemByCondition(PageQuery pageQuery) {
        return saleItemMapper.countItemsByCondition(pageQuery);
    }
    
    @Override
    public int saveItem(SaleItem saleItem) {
        // 将 domain model 转换为 DO
        SaleItemDO saleItemDO = InfraConverter.toSaleItemDO(saleItem);
        
        // 如果 商品ID 为空，则插入新的商品
        if (saleItem.getId() == null) {
            return saleItemMapper.insertItem(saleItemDO);
        }
        
        // 否则更新商品信息
        return saleItemMapper.updateItem(saleItemDO);
    }
    
    @Override
    public boolean deductStockForItem(Long itemId, Integer quantity) {
        // 扣减库存，并检查是否扣减成功
        return saleItemMapper.reduceStockById(itemId, quantity) == 1;
    }
    
    @Override
    public boolean revertStockForItem(Long itemId, Integer quantity) {
        // 恢复库存，并检查是否恢复成功
        return saleItemMapper.addStockById(itemId, quantity) == 1;
    }
}
