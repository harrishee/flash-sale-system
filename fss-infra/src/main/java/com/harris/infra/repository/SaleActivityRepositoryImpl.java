package com.harris.infra.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.repository.SaleActivityRepository;
import com.harris.infra.mapper.SaleActivityMapper;
import com.harris.infra.model.SaleActivityDO;
import com.harris.infra.model.converter.SaleActivityToDOConverter;
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
        SaleActivityDO saleActivityDO = saleActivityMapper.getActivityById(activityId);
        if (saleActivityDO == null) {
            return Optional.empty();
        }
        SaleActivity saleActivity = SaleActivityToDOConverter.toDomainModel(saleActivityDO);
        return Optional.of(saleActivity);
    }

    @Override
    public List<SaleActivity> findActivitiesByCondition(PageQueryCondition pageQueryCondition) {
        return saleActivityMapper.getActivitiesByCondition(pageQueryCondition)
                .stream()
                .map(SaleActivityToDOConverter::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countActivitiesByCondition(PageQueryCondition pageQueryCondition) {
        return saleActivityMapper.countActivitiesByCondition(pageQueryCondition);
    }

    @Override
    public int saveActivity(SaleActivity saleActivity) {
        SaleActivityDO saleActivityDO = SaleActivityToDOConverter.toDO(saleActivity);
        if (saleActivityDO.getId() == null) {
            int effectedRows = saleActivityMapper.insertActivity(saleActivityDO);
            saleActivity.setId(saleActivityDO.getId());
            return effectedRows;
        }
        return saleActivityMapper.updateActivity(saleActivityDO);
    }
}
