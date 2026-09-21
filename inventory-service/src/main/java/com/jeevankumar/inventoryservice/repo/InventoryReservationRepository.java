package com.jeevankumar.inventoryservice.repo;

import com.jeevankumar.inventoryservice.model.InventoryReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservationEntity, Long> {
    boolean existsByOrderId(String orderId);
    Optional<InventoryReservationEntity> findByOrderId(String orderId);
}
