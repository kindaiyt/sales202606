package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreService storeService;

    public ProductService(ProductRepository productRepository,
                          StoreService storeService) {
        this.productRepository = productRepository;
        this.storeService = storeService;
    }

    @Transactional
    public Product save(Product product) {
        Integer price = product.getPrice();
        if (price == null || product.getPrice() <= 0) {
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

    public List<Product> findByStoreSorted(Store store) {
        return productRepository.findByStoreOrderBySortOrderAscIdAsc(store);
    }

    @Transactional
    public void sortProducts(String url, List<Long> ids) {

        Store store = storeService.findByUrl(url);

        List<Product> products =
                productRepository.findByStore(store);

        Map<Long, Product> map = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (int i = 0; i < ids.size(); i++) {
            Product p = map.get(ids.get(i));

            if (p != null) {
                p.setSortOrder(i + 1);
            }
        }

        productRepository.saveAll(products);
    }

    public long countByStore(Store store) {
        return productRepository.countByStore(store);
    }

    @Transactional
    public void updateSortOrder(Store store, String orderedIds) {
        if (orderedIds == null || orderedIds.trim().isEmpty()) return;

        String[] ids = orderedIds.split(",");
        int order = 1;

        for (String idRaw : ids) {
            String s = idRaw == null ? null : idRaw.trim();
            if (s == null || s.isEmpty()) continue;

            Long id;
            try { id = Long.valueOf(s); } catch (Exception e) { continue; }

            Product p = productRepository.findById(id).orElse(null);
            if (p == null) continue;

            // ★ 他店のproductが混ざる事故対策
            if (p.getStore() == null || !p.getStore().getId().equals(store.getId())) continue;

            p.setSortOrder(order++);
            productRepository.save(p);
        }
    }

}
