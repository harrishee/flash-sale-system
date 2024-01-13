package com.harris.app.model.cache;

import com.harris.domain.model.entity.FlashActivity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FlashActivityCache {
    private FlashActivity flashActivity;
    private Long version;
    private boolean later;
    protected boolean exist;

    public FlashActivityCache with(FlashActivity flashActivity) {
        this.flashActivity = flashActivity;
        this.exist = true;
        return this;
    }

    public FlashActivityCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashActivityCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashActivityCache notExist() {
        this.exist = false;
        return this;
    }
}
