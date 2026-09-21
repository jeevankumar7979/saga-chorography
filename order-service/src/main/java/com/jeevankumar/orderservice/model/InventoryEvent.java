package com.jeevankumar.orderservice.model;

import java.time.Instant;

public record InventoryEvent(String orderId, InventoryStatus status, Instant timestamp) {
    public enum InventoryStatus {
        RESERVED,
        OUT_OF_STOCK,
        CANCELLED
    }
}
