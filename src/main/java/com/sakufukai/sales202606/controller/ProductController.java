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
        Store store = storeService.findByUrl(url);
        if (store == null) {
            throw new IllegalArgumentException("店舗が見つかりません: " + url);
        }
        model.addAttribute("store", store);
        return "product/add";
    }

    // 商品保存（URLを /add/{url} に統一）
    @PostMapping("/add/{url}")
    public String saveProduct(
            @PathVariable String url,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String note,
            Model model
    ) {
        Store store = storeService.findByUrl(url);
        if (store == null) {
            throw new IllegalArgumentException("店舗が見つかりません: " + url);
        }

        // 画面に値を戻す用
        model.addAttribute("store", store);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("note", note);

        boolean hasError = false;

        // 商品名チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "商品名を入力してください。");
            hasError = true;
        }

        // 価格チェック
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
                model.addAttribute("priceError",
                        "価格は1以上の整数で入力してください。<br>また、2147483647より大きい価格は設定できません。");
                hasError = true;
            }
        }

        if (hasError) {
            // URLは /product/add/{url} のまま
            return "product/add";
        }

        Product product = new Product();
        product.setName(name.trim());
        product.setPrice(priceInt);
        product.setNote(note);
        product.setStore(store);

        productService.save(product);
        return "redirect:/store/" + url;
    }

    // =========================
    // 削除
    // =========================
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        String storeUrl = product.getStore().getUrl();
        productService.deleteById(id);
        return "redirect:/store/" + storeUrl;
    }

    // =========================
    // 編集・更新
    // =========================

    // 編集ページ表示
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product/edit";
    }

    // 更新処理（URLを /edit/{id} に統一）
    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String note,
            Model model
    ) {
        Product product = productService.findById(id);

        // 画面に戻す用（※ product 自体は必須）
        model.addAttribute("product", product);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("note", note);

        boolean hasError = false;

        // 商品名チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "商品名を入力してください。");
            hasError = true;
        }

        // 価格チェック
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
                model.addAttribute("priceError",
                        "価格は1以上の整数で入力してください。<br>また、2147483647より大きい価格は設定できません。");
                hasError = true;
            }
        }

        if (hasError) {
            // URLは /product/edit/{id} のまま
            return "product/edit";
        }

        product.setName(name.trim());
        product.setPrice(priceInt);
        product.setNote(note);

        productService.save(product);
        return "redirect:/store/" + product.getStore().getUrl();
    }

    // =========================
    // 並べ替え
    // =========================
    @GetMapping("/sort/{url}")
    public String sortProductsPage(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url);
        model.addAttribute("store", store);
        model.addAttribute("products", productService.findByStoreSorted(store));
        return "product/sort";
    }

    @PostMapping("/sort/{url}")
    public String saveProductsSort(@PathVariable String url,
                                   @RequestParam String orderedIds) {
        Store store = storeService.findByUrl(url);
        productService.updateSortOrder(store, orderedIds);
        return "redirect:/store/" + url;
    }
}
