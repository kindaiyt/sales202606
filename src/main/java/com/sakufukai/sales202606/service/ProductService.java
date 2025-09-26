package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }
}
