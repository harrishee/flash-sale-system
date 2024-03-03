package com.harris.infra.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.repository.SaleActivityRepository;
import com.harris.infra.mapper.SaleActivityMapper;
import com.harris.infra.model.SaleActivityDO;
import com.harris.infra.util.InfraConverter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SaleActivityRepositoryImpl implements SaleActivityRepository {
    @Resource
    private SaleActivityMapper saleActivityMapper;
    
    @Override
    public Optional<SaleActivity> findActivityById(Long activityId) {
        // 从 mapper 中获取 DO
        SaleActivityDO saleActivityDO = saleActivityMapper.getActivityById(activityId);
        if (saleActivityDO == null) {
            return Optional.empty();
        }
        
        // 将 DO 转换为 domain model
        SaleActivity saleActivity = InfraConverter.toSaleActivityDomain(saleActivityDO);
        return Optional.of(saleActivity);
    }
    
    @Override
    public List<SaleActivity> findAllActivityByCondition(PageQuery pageQuery) {
        // 从 mapper 中获取 DO 列表，然后转换为 domain model 列表
        return saleActivityMapper.getActivitiesByCondition(pageQuery)
                .stream()
                .map(InfraConverter::toSaleActivityDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer countAllActivityByCondition(PageQuery pageQuery) {
        // 从 mapper 中获取符合条件的活动数量
        return saleActivityMapper.countActivitiesByCondition(pageQuery);
    }
    
    @Override
    public int saveActivity(SaleActivity saleActivity) {
        // 将 domain model 转换为 DO
        SaleActivityDO saleActivityDO = InfraConverter.toSaleActivityDO(saleActivity);
        
        // 如果 活动ID 为空，则插入新的活动，并将 ID 设置到 domain model 中
        if (saleActivityDO.getId() == null) {
            int effectedRows = saleActivityMapper.insertActivity(saleActivityDO);
            saleActivity.setId(saleActivityDO.getId());
            return effectedRows;
        }
        
        // 否则更新活动
        return saleActivityMapper.updateActivity(saleActivityDO);
    }
}
