package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.service.ProductService;
import com.sakufukai.sales202606.service.StoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String note,
            Model model
    ) {
        Store store = storeService.findByUrl(storeUrl);

        // 画面に値を戻す用
        model.addAttribute("store", store);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("note", note);

        boolean hasError = false;

        // サーバ側バリデーション: 商品名
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "商品名を入力してください。");
            hasError = true;
        }

        // サーバ側バリデーション: 価格
        Integer priceInt = null;

        if (price == null || price.trim().isEmpty()) {
            model.addAttribute("priceError", "価格を入力してください。");
            hasError = true;
        } else {
            try {
                priceInt = Integer.valueOf(price.trim());
                if (priceInt <= 0) {
                    model.addAttribute("priceError", "価格は1以上の整数で入力してください。");
                    hasError = true;
                }
            } catch (Exception e) {
                model.addAttribute("priceError", "価格は整数で入力してください。");
                hasError = true;
            }
        }

        if (hasError) {
            return "product/add";
        }

        Product product = new Product();
        product.setName(name.trim());
        product.setPrice(priceInt);
        product.setNote(note);
        product.setStore(store);

        productService.save(product);
        return "redirect:/store/" + storeUrl;
    }

    // 商品削除処理
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productService.findById(id);

        String storeUrl = product.getStore().getUrl();
        productService.deleteById(id);

        return "redirect:/store/" + storeUrl;
    }

    // 編集ページ表示
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product/edit"; // templates/product/edit.html
    }

    // 商品更新処理
    @PostMapping("/update")
    public String updateProduct(
            @RequestParam Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String note,
            Model model
    ) {
        Product product = productService.findById(id);

        // 画面に戻す用
        model.addAttribute("product", product);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("note", note);

        boolean hasError = false;

        // サーバ側バリデーション: 商品名
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "商品名を入力してください。");
            hasError = true;
        }

        // サーバ側バリデーション: 価格
        Integer priceInt = null;

        if (price == null || price.trim().isEmpty()) {
            model.addAttribute("priceError", "価格を入力してください。");
            hasError = true;
        } else {
            try {
                priceInt = Integer.valueOf(price.trim());
                if (priceInt <= 0) {
                    model.addAttribute("priceError", "価格は1以上の整数で入力してください。");
                    hasError = true;
                }
            } catch (Exception e) {
                model.addAttribute("priceError", "価格は整数で入力してください。");
                hasError = true;
            }
        }

        if (hasError) {
            return "product/edit";
        }

        product.setName(name.trim());
        product.setPrice(priceInt);
        product.setNote(note);

        productService.save(product);
        return "redirect:/store/" + product.getStore().getUrl();
    }

    // ProductController に追加
    @GetMapping("/sort/{url}")
    public String sortProductsPage(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url);
        model.addAttribute("store", store);
        model.addAttribute("products", productService.findByStoreSorted(store)); // ★ sorted
        return "product/sort";
    }

    @PostMapping("/sort/{url}")
    public String saveProductsSort(@PathVariable String url,
                                   @RequestParam String orderedIds) {
        Store store = storeService.findByUrl(url);
        productService.updateSortOrder(store, orderedIds); // ★ id の並びで保存
        return "redirect:/store/" + url;
    }

}
