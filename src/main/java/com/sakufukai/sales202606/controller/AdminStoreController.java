package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
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
        return "admin/store-form";
    }

    // 店舗保存（POST） ★ /new に寄せる
    @PostMapping("/new")
    public String saveStore(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String url,
                            Model model) {

        model.addAttribute("name", name);
        model.addAttribute("url", url);

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

        if (hasError) {
            // ★ URLは /admin/stores/new のまま
            return "admin/store-form";
        }

        Store store = new Store();
        store.setName(name.trim());
        store.setUrl(trimmedUrl);

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
