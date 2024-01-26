package com.harris.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkUtil {
    public static String link(Object... items) {
        if (items == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        // Concatenate the items with an underscore between them
        for (int i = 0; i < items.length; i++) {
            sb.append((items[i]));
            if (i < items.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}
