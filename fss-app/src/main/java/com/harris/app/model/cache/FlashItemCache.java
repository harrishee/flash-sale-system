package com.harris.app.model.cache;

import com.harris.domain.model.entity.FlashItem;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FlashItemCache {
    private FlashItem flashItem;
    private Long version;
    private boolean later;
    protected boolean exist;

    public FlashItemCache with(FlashItem flashItem) {
        this.flashItem = flashItem;
        this.exist = true;
        return this;
    }

    public FlashItemCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashItemCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashItemCache notExist() {
        this.exist = false;
        return this;
    }
}
