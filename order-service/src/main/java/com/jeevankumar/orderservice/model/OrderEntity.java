package com.jeevankumar.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    public enum Stage {
        PENDING,
        PAYMENT_CONFIRMED, PAYMENT_FAILED,
        INVENTORY_RESERVED, INVENTORY_FAILED, INVENTORY_CANCELLED,
        COMPLETED, CANCELLED, FAILED
    }

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage paymentStage = Stage.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage inventoryStage = Stage.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected OrderEntity() {
        // required by JPA
    }

    public OrderEntity(String orderId, String customerId, String productId, int quantity, BigDecimal amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Stage getPaymentStage() {
        return paymentStage;
    }

    public Stage getInventoryStage() {
        return inventoryStage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setPaymentStage(Stage s) {
        this.paymentStage = s;
    }

    public void setInventoryStage(Stage s) {
        this.inventoryStage = s;
    }

    /**
     * Overall status derived from the two independent branches of the saga.
     * PAYMENT_FAILED is treated as CANCELLED (distinct from FAILED) because
     * it triggers a compensating action in inventory-service — the order
     * didn't just fail, it was actively unwound.
     */
    public Stage overallStatus() {
        if (paymentStage == Stage.PAYMENT_FAILED) {
            return Stage.CANCELLED;
        }
        if (inventoryStage == Stage.INVENTORY_FAILED) {
            return Stage.FAILED;
        }
        if (paymentStage == Stage.PAYMENT_CONFIRMED && inventoryStage == Stage.INVENTORY_RESERVED) {
            return Stage.COMPLETED;
        }
        return Stage.PENDING;
    }
}