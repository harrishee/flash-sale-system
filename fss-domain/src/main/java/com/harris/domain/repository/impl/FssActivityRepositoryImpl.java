package com.harris.domain.repository.impl;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashActivity;
import com.harris.domain.repository.FssActivityRepository;
import com.harris.infra.persistence.converter.FssActivityConverter;
import com.harris.infra.persistence.mapper.FssActivityMapper;
import com.harris.infra.persistence.model.FlashActivityDO;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FssActivityRepositoryImpl implements FssActivityRepository {
    @Resource
    private FssActivityMapper fssActivityMapper;

    @Override
    public int saveActivity(FlashActivity flashActivity) {
        FlashActivityDO flashActivityDO = FssActivityConverter.toDataObject(flashActivity);
        if (flashActivityDO.getId() == null) {
            int effectedRows = fssActivityMapper.insertActivity(flashActivityDO);
            flashActivity.setId(flashActivityDO.getId());
            return effectedRows;
        }
        return fssActivityMapper.updateActivity(flashActivityDO);
    }

    @Override
    public Optional<FlashActivity> findActivityById(Long activityId) {
        FlashActivityDO flashActivityDO = fssActivityMapper.getActivityById(activityId);
        if (flashActivityDO == null) {
            return Optional.empty();
        }
        FlashActivity flashActivity = FssActivityConverter.toDomainObject(flashActivityDO);
        return Optional.of(flashActivity);
    }

    @Override
    public List<FlashActivity> findActivitiesByCondition(PagesQueryCondition pagesQueryCondition) {
        return fssActivityMapper.getActivitiesByCondition(pagesQueryCondition)
                .stream()
                .map(FssActivityConverter::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countActivitiesByCondition(PagesQueryCondition pagesQueryCondition) {
        return fssActivityMapper.countActivitiesByCondition(pagesQueryCondition);
    }
}
