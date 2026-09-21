package com.jeevankumar.inventoryservice.model;

import java.time.Instant;

public record PaymentEvent(String orderId, String paymentId, PaymentStatus status, Instant timestamp) {
    public enum PaymentStatus {
        PAID,
        FAILED,
        REFUNDED
    }
}
