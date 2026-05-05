package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.StoreType;
import com.sakufukai.sales202606.service.StoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

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
    public String listStores(Model model, HttpSession session) {

        List<Store> stores = storeService.findAll();

        String sort = (String) session.getAttribute("storeSort"); // "name" or "url"
        String dir  = (String) session.getAttribute("storeDir");  // "asc" or "desc"

        if ("name".equals(sort)) {
            stores = stores.stream()
                    .sorted((a, b) -> {
                        String an = a.getName() == null ? "" : a.getName();
                        String bn = b.getName() == null ? "" : b.getName();
                        int cmp = an.compareToIgnoreCase(bn);
                        return "desc".equals(dir) ? -cmp : cmp;
                    })
                    .toList();

        } else if ("url".equals(sort)) {
            stores = stores.stream()
                    .sorted((a, b) -> {
                        String au = a.getUrl() == null ? "" : a.getUrl();
                        String bu = b.getUrl() == null ? "" : b.getUrl();
                        int cmp = au.compareToIgnoreCase(bu);
                        return "desc".equals(dir) ? -cmp : cmp;
                    })
                    .toList();

        } else if ("storeType".equals(sort)) {
            stores = stores.stream()
                    .sorted((a, b) -> {
                        String at = a.getStoreType() == null ? "" : a.getStoreType().name();
                        String bt = b.getStoreType() == null ? "" : b.getStoreType().name();
                        int cmp = at.compareToIgnoreCase(bt);
                        return "desc".equals(dir) ? -cmp : cmp;
                    })
                    .toList();
        }

        model.addAttribute("stores", stores);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        return "admin/stores";
    }

    // 店舗作成フォーム（GET）
    @GetMapping("/new")
    public String newStoreForm(Model model) {
        model.addAttribute("name", "");
        model.addAttribute("url", "");
        model.addAttribute("locationName", "");
        model.addAttribute("storeType", "STUDENT");
        return "admin/store-form";
    }

    // 店舗保存（POST） ★ /new に寄せる
    @PostMapping("/new")
    public String saveStore(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String url,
                            @RequestParam(required = false) String locationName,
                            @RequestParam(required = false) String storeType,
                            Model model) {

        model.addAttribute("name", name);
        model.addAttribute("url", url);
        model.addAttribute("locationName", locationName);
        model.addAttribute("storeType", storeType);

        boolean hasError = false;

        // 店舗名チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "店舗名を入力してください。");
            hasError = true;
        }

        // URLチェック
        String trimmedUrl = (url == null) ? null : url.trim();
        if (trimmedUrl == null || trimmedUrl.isEmpty()) {
            model.addAttribute("urlError", "店舗URLを入力してください。");
            hasError = true;
        } else {
            if (storeService.existsByUrl(trimmedUrl)) {
                model.addAttribute("urlError", "このURLは既に存在します。");
                hasError = true;
            }
        }

        // 店舗種別 ← ★追加
        StoreType type = null;
        if (storeType == null || storeType.isBlank()) {
            model.addAttribute("storeTypeError", "店舗種別を選択してください。");
            hasError = true;
        } else {
            try {
                type = StoreType.valueOf(storeType);
            } catch (Exception e) {
                model.addAttribute("storeTypeError", "不正な店舗種別です。");
                hasError = true;
            }
        }

        if (hasError) {
            // ★ URLは /admin/stores/new のまま
            return "admin/store-form";
        }

        Store store = new Store();
        store.setName(name.trim());
        store.setUrl(trimmedUrl);
        store.setLocationName(
                locationName == null || locationName.trim().isEmpty()
                        ? null
                        : locationName.trim()
        );
        store.setStoreType(type);

        try {
            storeService.save(store);
        } catch (IllegalArgumentException e) {
            model.addAttribute("urlError", e.getMessage());
            return "admin/store-form";
        }

        return "redirect:/admin/stores";
    }

    // 店舗削除
    @PostMapping("/{url}/delete")
    public String deleteStore(@PathVariable String url) {
        storeService.deleteStoreByUrl(url);
        return "redirect:/admin/stores";
    }

    @PostMapping("/view-sort")
    public String sortStores(@RequestParam String sort,
                             @RequestParam String dir,
                             HttpSession session) {

        if (!("name".equals(sort) || "url".equals(sort) || "storeType".equals(sort))) {
            return "redirect:/admin/stores";
        }

        if (!("asc".equals(dir) || "desc".equals(dir))) {
            dir = "asc";
        }

        session.setAttribute("storeSort", sort);
        session.setAttribute("storeDir", dir);

        return "redirect:/admin/stores";
    }

    @PostMapping("/view-sort/clear")
    public String clearSort(HttpSession session) {

        session.removeAttribute("storeSort");
        session.removeAttribute("storeDir");

        return "redirect:/admin/stores";
    }

}
