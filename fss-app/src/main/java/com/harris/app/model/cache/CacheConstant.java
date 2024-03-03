package com.harris.app.model.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConstant {
    public static final Long MINUTES_5 = 5 * 60L;
    public static final Long HOURS_24 = 3600 * 24L;
    public static final String ACTIVITY_CACHE_KEY = "ACTIVITY_CACHE_KEY";
    public static final String ACTIVITIES_CACHE_KEY = "ACTIVITIES_CACHE_KEY";
    public static final String ITEM_CACHE_KEY = "ITEM_CACHE_KEY";
    public static final String ITEMS_CACHE_KEY = "ITEMS_CACHE_KEY";
}
