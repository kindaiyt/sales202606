package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
