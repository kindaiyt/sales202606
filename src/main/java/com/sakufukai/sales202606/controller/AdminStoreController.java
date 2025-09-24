package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.service.StoreService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/stores")
public class AdminStoreController {

    private final StoreService storeService;

    public AdminStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // 店舗一覧
    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = storeService.findAll();
        model.addAttribute("stores", stores);
        return "admin/stores"; // admin/stores.html
    }

    // 店舗作成フォーム
    @GetMapping("/new")
    public String newStoreForm(Model model) {
        model.addAttribute("store", new Store());
        return "admin/store-form"; // admin/store_form.html
    }

    // 店舗保存
    @PostMapping
    public String saveStore(@ModelAttribute Store store, Model model) {
        try {
            storeService.save(store);
            return "redirect:/admin/stores";
        } catch (IllegalArgumentException e) {
            model.addAttribute("store", store);
            model.addAttribute("error", e.getMessage());
            return "admin/store-form"; // エラー時はフォームに戻す
        }
    }


    // 店舗削除
    @PostMapping("/{id}/delete")
    public String deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return "redirect:/admin/stores";
    }
}
