package com.harris.domain.event.bucket;

import com.alibaba.cola.event.DomainEventI;

import java.util.Objects;

public class BucketEvent implements DomainEventI {
    private Long itemId;
    private BucketEventType bucketEventType;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BucketEventType getBucketEventType() {
        return bucketEventType;
    }

    public void setBucketEventType(BucketEventType bucketEventType) {
        this.bucketEventType = bucketEventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, bucketEventType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketEvent that = (BucketEvent) o;
        return Objects.equals(itemId, that.itemId) && bucketEventType == that.bucketEventType;
    }

    @Override
    public String toString() {
        return "BucketEvent{" +
                "itemId=" + itemId +
                ", bucketEventType=" + bucketEventType +
                '}';
    }
}
