package com.jeevankumar.inventoryservice.repo;

import com.jeevankumar.inventoryservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
