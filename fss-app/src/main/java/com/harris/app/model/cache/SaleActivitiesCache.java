package com.harris.app.model.cache;

import com.harris.domain.model.entity.SaleActivity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SaleActivitiesCache {
    private List<SaleActivity> saleActivities;
    private Integer total;
    private Long version;
    private boolean later;
    protected boolean exist;

    public SaleActivitiesCache with(List<SaleActivity> flashActivities) {
        this.saleActivities = flashActivities;
        this.exist = true;
        return this;
    }

    public SaleActivitiesCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public SaleActivitiesCache tryLater() {
        this.later = true;
        return this;
    }

    public SaleActivitiesCache notExist() {
        this.exist = false;
        return this;
    }
}
