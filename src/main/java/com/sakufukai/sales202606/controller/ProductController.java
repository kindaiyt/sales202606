package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.service.ProductService;
import com.sakufukai.sales202606.service.StoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final StoreService storeService;
    private final ProductService productService;

    public ProductController(StoreService storeService, ProductService productService) {
        this.storeService = storeService;
        this.productService = productService;
    }

    // 商品追加ページを表示
    @GetMapping("/add/{url}")
    public String addProductForm(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url); // findByUrl は Store を返す前提
        if (store == null) {
            throw new IllegalArgumentException("店舗が見つかりません: " + url);
        }

        model.addAttribute("store", store);
        return "product/add"; // product/add.html を表示
    }

    // 商品保存処理
    @PostMapping("/save")
    public String saveProduct(
            @RequestParam String storeUrl,
            @RequestParam String name,
            @RequestParam double price
    ) {
        Store store = storeService.findByUrl(storeUrl);
        if (store == null) {
            throw new IllegalArgumentException("店舗が見つかりません: " + storeUrl);
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStore(store);

        productService.save(product);

        // 商品追加後、店舗ページにリダイレクト
        return "redirect:/store/" + storeUrl;
    }
}
