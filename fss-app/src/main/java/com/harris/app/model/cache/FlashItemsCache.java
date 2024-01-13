package com.harris.app.model.cache;

import com.harris.domain.model.entity.FlashItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FlashItemsCache {
    private List<FlashItem> flashItems;
    private Integer total;
    private Long version;
    private boolean later;
    protected boolean exist;

    public FlashItemsCache with(List<FlashItem> flashItems) {
        this.flashItems = flashItems;
        this.exist = true;
        return this;
    }

    public FlashItemsCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashItemsCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashItemsCache notExist() {
        this.exist = false;
        return this;
    }
}
