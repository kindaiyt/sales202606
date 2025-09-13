package com.sakufukai.sales202606.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StoreController {

    @GetMapping("/store/{storeName}")
    public String storePage(@PathVariable String storeName) {
        // ここで店舗ごとのデータをDBから取ってくるようにする
        return "store"; // store.html に飛ばす
    }
}
