package com.harris.app.model.cache;

import com.harris.domain.model.entity.FlashActivity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FlashActivitiesCache {
    private List<FlashActivity> flashActivities;
    private Integer total;
    private Long version;
    private boolean later;
    protected boolean exist;

    public FlashActivitiesCache with(List<FlashActivity> flashActivities) {
        this.flashActivities = flashActivities;
        this.exist = true;
        return this;
    }

    public FlashActivitiesCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashActivitiesCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashActivitiesCache notExist() {
        this.exist = false;
        return this;
    }
}
