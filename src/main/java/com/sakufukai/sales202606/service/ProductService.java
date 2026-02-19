package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

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

    // ★追加：表示のみソート（DB順は変更しない）
    public List<Product> findByStoreSorted(Store store, String sort, String dir) {
        List<Product> base = findByStoreSorted(store); // まずDBの並び（sortOrder）を基準に取る

        if (sort == null || sort.isBlank()) return base;

        boolean asc = !"desc".equalsIgnoreCase(dir);

        Comparator<Product> comparator;
        switch (sort) {
            case "name":
                comparator = Comparator.comparing(
                        p -> p.getName() == null ? "" : p.getName(),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;
            case "price":
                comparator = Comparator.comparing(
                        p -> p.getPrice() == null ? 0 : p.getPrice()
                );
                break;
            default:
                return base;
        }

        if (!asc) comparator = comparator.reversed();

        // 同値のときに順番がブレないように、最後に id を足す（安定化）
        comparator = comparator.thenComparing(Product::getId);

        return base.stream().sorted(comparator).toList();
    }

    /**
     * 認可込み：保存（追加/更新の両方で使える）
     */
    @Transactional
    public Product saveForMember(Product product, Authentication authentication) {
        if (product == null) {
            throw new IllegalArgumentException("product is null");
        }
        if (product.getStore() == null) {
            throw new IllegalArgumentException("store is null");
        }

        // ★所属 or ADMIN
        storeService.assertMemberOrAdmin(product.getStore(), authentication);

        // 既存のバリデーション
        Integer price = product.getPrice();
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("価格は1以上の整数で入力してください。");
        }

        return productRepository.save(product);
    }

    /**
     * 認可込み：削除（/product/delete/{id} 用）
     */
    @Transactional
    public void deleteByIdForMember(Long id, Authentication authentication) {
        Product product = findById(id);
        if (product.getStore() == null) {
            throw new IllegalArgumentException("store is null");
        }

        // ★所属 or ADMIN
        storeService.assertMemberOrAdmin(product.getStore(), authentication);

        productRepository.deleteById(id);
    }

    /**
     * 認可込み：ドラッグ並べ替え保存（/product/sort/{url} の POST 用）
     */
    @Transactional
    public void updateSortOrderForMember(Store store, String orderedIds, Authentication authentication) {
        if (store == null) throw new IllegalArgumentException("store is null");

        // ★所属 or ADMIN
        storeService.assertMemberOrAdmin(store, authentication);

        // 既存ロジックへ委譲
        updateSortOrder(store, orderedIds);
    }

    /**
     * 認可込み：旧 sortProducts(url, ids) を使う場合のラッパ
     */
    @Transactional
    public void sortProductsForMember(String url, List<Long> ids, Authentication authentication) {
        Store store = storeService.getStoreForMemberOrAdmin(url, authentication);
        // 既存ロジックへ委譲（store 取得済みなので store から取り直さない版にしてもOK）
        sortProducts(url, ids);
    }

}
