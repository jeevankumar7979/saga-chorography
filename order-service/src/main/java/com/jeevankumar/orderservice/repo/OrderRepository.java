package com.jeevankumar.orderservice.repo;

import com.jeevankumar.orderservice.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    /**
     * Atomic, single-column update. This is deliberate: payment-event and
     * inventory-event for the same order can arrive on different consumer
     * threads within milliseconds of each other. If both did
     * findById(...) -> mutate one field -> save(wholeEntity), whichever
     * save() lands second overwrites the other's field with its own stale
     * in-memory copy (a classic lost-update race). Updating exactly one
     * column at the DB level makes the two writes commute safely no matter
     * which order they happen in.
     */
    @Modifying
    @Query("UPDATE OrderEntity o SET o.paymentStage = :stage WHERE o.orderId = :orderId")
    int updatePaymentStage(@Param("orderId") String orderId, @Param("stage") OrderEntity.Stage stage);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.inventoryStage = :stage WHERE o.orderId = :orderId")
    int updateInventoryStage(@Param("orderId") String orderId, @Param("stage") OrderEntity.Stage stage);
}
