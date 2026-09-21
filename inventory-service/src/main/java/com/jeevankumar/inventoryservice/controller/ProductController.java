package com.jeevankumar.inventoryservice.controller;

import com.jeevankumar.inventoryservice.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public record ProductResponse(String productId, Integer availabilityQuantity) {}

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> new ProductResponse(product.getProductId(), product.getAvailableQuantity()))
                .toList();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        return productRepository.findById(productId)
                .map(product -> ResponseEntity.ok(new ProductResponse(product.getProductId(), product.getAvailableQuantity())))
                .orElse(ResponseEntity.notFound().build());
    }
}
