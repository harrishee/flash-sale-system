package com.harris.infra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for creating concatenated strings.
 * This class provides a static method to concatenate various objects
 * into a single string, separated by underscores.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyUtil {
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
