package com.harris.domain.repository.impl;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashActivity;
import com.harris.domain.repository.FlashActivityRepository;
import com.harris.infra.persistence.converter.FlashActivityConverter;
import com.harris.infra.persistence.mapper.FlashActivityMapper;
import com.harris.infra.persistence.model.FlashActivityDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlashActivityRepositoryImpl implements FlashActivityRepository {
    @Resource
    private FlashActivityMapper flashActivityMapper;

    @Override
    public int saveActivity(FlashActivity flashActivity) {
        FlashActivityDO flashActivityDO = FlashActivityConverter.toDO(flashActivity);
        if (flashActivityDO.getId() == null) {
            int effectedRows = flashActivityMapper.insertActivity(flashActivityDO);
            flashActivity.setId(flashActivityDO.getId());
            return effectedRows;
        }
        return flashActivityMapper.updateActivity(flashActivityDO);
    }

    @Override
    public Optional<FlashActivity> findActivityById(Long activityId) {
        FlashActivityDO flashActivityDO = flashActivityMapper.getActivityById(activityId);
        if (flashActivityDO == null) {
            return Optional.empty();
        }
        FlashActivity flashActivity = FlashActivityConverter.toDomainObj(flashActivityDO);
        return Optional.of(flashActivity);
    }

    @Override
    public List<FlashActivity> findActivitiesByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashActivityMapper.getActivitiesByCondition(pagesQueryCondition)
                .stream()
                .map(FlashActivityConverter::toDomainObj)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countActivitiesByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashActivityMapper.countActivitiesByCondition(pagesQueryCondition);
    }
}
