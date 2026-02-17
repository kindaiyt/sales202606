package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product save(Product product) {
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("価格は1以上の整数で入力してください。");
        }
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
