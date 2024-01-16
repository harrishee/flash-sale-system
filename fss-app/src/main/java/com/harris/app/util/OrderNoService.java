package com.harris.app.util;

import com.harris.app.model.OrderNoContext;

public interface OrderNoService {
    Long generateOrderNo(OrderNoContext orderNoContext);
}
