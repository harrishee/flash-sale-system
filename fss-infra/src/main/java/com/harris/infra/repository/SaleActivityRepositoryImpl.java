package com.harris.infra.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.repository.SaleActivityRepository;
import com.harris.infra.mapper.SaleActivityMapper;
import com.harris.infra.model.SaleActivityDO;
import com.harris.infra.model.converter.SaleActivityConverter;
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
        // Get the DO from the mapper and validate
        SaleActivityDO saleActivityDO = saleActivityMapper.getActivityById(activityId);
        if (saleActivityDO == null) {
            return Optional.empty();
        }

        // Convert the DO to a domain model
        SaleActivity saleActivity = SaleActivityConverter.toDomainModel(saleActivityDO);
        return Optional.of(saleActivity);
    }

    @Override
    public List<SaleActivity> findActivitiesByCondition(PageQuery pageQuery) {
        // Get the DOs from the mapper and convert to domain models
        return saleActivityMapper.getActivitiesByCondition(pageQuery)
                .stream()
                .map(SaleActivityConverter::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countActivitiesByCondition(PageQuery pageQuery) {
        return saleActivityMapper.countActivitiesByCondition(pageQuery);
    }

    @Override
    public int saveActivity(SaleActivity saleActivity) {
        // Convert the domain model to a DO
        SaleActivityDO saleActivityDO = SaleActivityConverter.toDO(saleActivity);

        // If the ID is null, insert the new activity with its ID
        if (saleActivityDO.getId() == null) {
            int effectedRows = saleActivityMapper.insertActivity(saleActivityDO);
            saleActivity.setId(saleActivityDO.getId());
            return effectedRows;
        }

        // Otherwise, update the existed activity
        return saleActivityMapper.updateActivity(saleActivityDO);
    }
}
