package com.jeevankumar.inventoryservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String productId;

    @Column(nullable = false)
    private Integer availableQuantity;

    protected Product() {
        // required by JPA
    }

    public Product(String productId, Integer availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public String getProductId() { return productId; }
    public Integer getAvailableQuantity() { return availableQuantity; }

    public boolean hasStock(Integer requested) {
        return availableQuantity >= requested;
    }

    public void reserve(Integer quantity) {
        this.availableQuantity -= quantity;
    }

    public void release(Integer quantity) {
        this.availableQuantity += quantity;
    }
}
