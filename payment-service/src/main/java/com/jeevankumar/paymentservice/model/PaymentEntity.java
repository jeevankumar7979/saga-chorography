package com.jeevankumar.paymentservice.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(columnNames = "orderId"))
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentEvent.PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PaymentEntity() {
        // required by JPA
    }

    public PaymentEntity(String orderId, String paymentId, BigDecimal amount, PaymentEvent.PaymentStatus status) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentEvent.PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentEvent.PaymentStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
