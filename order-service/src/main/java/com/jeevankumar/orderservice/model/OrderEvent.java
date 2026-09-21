package com.jeevankumar.orderservice.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent(String orderId, String customerId, String productId, Integer quantity, BigDecimal amount, Instant timestamp) {
    public static OrderEvent of(String orderId, String customerId, String productId, Integer quantity, BigDecimal amount) {
        return new OrderEvent(orderId, customerId, productId, quantity, amount, Instant.now());
    }
}
