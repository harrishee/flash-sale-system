package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;

public interface FlashItemDomainService {
    FlashItem getItem(Long itemId);

    PageResult<FlashItem> getItems(PagesQueryCondition pagesQueryCondition);

    void publishItem(FlashItem flashItem);

    void onlineItem(Long itemId);

    void offlineItem(Long itemId);
}
