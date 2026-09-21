package com.jeevankumar.paymentservice.repo;

import com.jeevankumar.paymentservice.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    boolean existsByOrderId(String orderId);
    Optional<PaymentEntity> findByOrderId(String orderId);
}
