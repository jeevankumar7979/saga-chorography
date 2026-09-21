package com.jeevankumar.inventoryservice.config;

import com.jeevankumar.inventoryservice.model.Product;
import com.jeevankumar.inventoryservice.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seed("sku-001", 100);
        seed("sku-002", 5);

    }

    private void seed(String productId, int quantity) {
        if(productRepository.existsById(productId)) {
            return;
        }
        productRepository.save(new Product(productId, quantity));
        log.info("Product {} has been seeded", productId);
    }
}
