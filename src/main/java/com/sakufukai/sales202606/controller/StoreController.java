package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StoreController {

    private final StoreRepository storeRepository;

    public StoreController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping("/store/{storeName}")
    public String storePage(@PathVariable String storeName, Model model) {
        // 店舗名で検索
        Store store = storeRepository.findByName(storeName).orElse(null);

        if (store != null) {
            model.addAttribute("store", store);
        } else {
            model.addAttribute("store", null);
        }

        return "store"; // store.html に飛ばす
    }
}
