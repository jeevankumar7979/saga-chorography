package com.jeevankumar.paymentservice.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent(String orderId, String customerId, String productId, Integer quantity, BigDecimal amount, Instant timestamp) {

}
