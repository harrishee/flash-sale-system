package com.harris.app.model;

import lombok.Data;

/**
 * Context for generating order numbers
 * This class is designed to hold information that might be needed for creating a customized order number
 * Currently, it contains the user ID, but it can be expanded to include more details if required in the future
 */
@Data
public class OrderNoContext {
    private Long userId;
}
