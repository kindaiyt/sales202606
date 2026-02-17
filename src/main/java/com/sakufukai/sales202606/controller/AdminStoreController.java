package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.service.StoreService;
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
        return "admin/stores";
    }

    // 店舗作成フォーム
    @GetMapping("/new")
    public String newStoreForm(Model model) {
        // 初期表示では空でOK
        model.addAttribute("name", "");
        model.addAttribute("url", "");
        return "admin/store-form";
    }

    // 店舗保存（新規作成）
    @PostMapping
    public String saveStore(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String url,
                            Model model) {

        // 入力値保持
        model.addAttribute("name", name);
        model.addAttribute("url", url);

        boolean hasError = false;

        // 店舗名チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "店舗名を入力してください。");
            hasError = true;
        }

        // URLチェック（空チェック）
        String trimmedUrl = (url == null) ? null : url.trim();
        if (trimmedUrl == null || trimmedUrl.isEmpty()) {
            model.addAttribute("urlError", "店舗URLを入力してください。");
            hasError = true;
        } else {
            // ★ 追加: URL重複チェック（name が空でもここは実行される）
            Store existing = storeService.findByUrl(trimmedUrl);
            if (existing != null) {
                model.addAttribute("urlError", "このURLは既に存在します。");
                hasError = true;
            }
        }

        if (hasError) {
            return "admin/store-form";
        }

        Store store = new Store();
        store.setName(name.trim());
        store.setUrl(trimmedUrl);

        // ここは基本通らない想定（競合対策として残してOK）
        try {
            storeService.save(store);
        } catch (IllegalArgumentException e) {
            model.addAttribute("urlError", e.getMessage());
            return "admin/store-form";
        }

        return "redirect:/admin/stores";
    }

    // 店舗削除
    @PostMapping("/{id}/delete")
    public String deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return "redirect:/admin/stores";
    }
}
