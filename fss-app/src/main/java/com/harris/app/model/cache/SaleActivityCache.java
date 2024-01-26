package com.harris.app.model.cache;

import com.harris.domain.model.entity.SaleActivity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SaleActivityCache {
    private SaleActivity saleActivity;
    private Long version;
    private boolean later;
    protected boolean exist;

    public SaleActivityCache with(SaleActivity saleActivity) {
        this.saleActivity = saleActivity;
        this.exist = true;
        return this;
    }

    public SaleActivityCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public SaleActivityCache tryLater() {
        this.later = true;
        return this;
    }

    public SaleActivityCache notExist() {
        this.exist = false;
        return this;
    }
}
