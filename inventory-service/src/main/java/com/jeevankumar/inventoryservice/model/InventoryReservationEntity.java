package com.jeevankumar.inventoryservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "inventory_reservations", uniqueConstraints = @UniqueConstraint(columnNames = "orderId"))
public class InventoryReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryEvent.InventoryStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InventoryReservationEntity() {
        // required by JPA
    }

    public InventoryReservationEntity(String orderId, String productId, int quantity, InventoryEvent.InventoryStatus status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public InventoryEvent.InventoryStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(InventoryEvent.InventoryStatus status) { this.status = status; }
}
