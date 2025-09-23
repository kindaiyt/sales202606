package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.repository.StoreRepository;
import com.sakufukai.sales202606.service.StoreService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/create")
    public String createStore(@AuthenticationPrincipal OidcUser principal,
                              @RequestParam String name,
                              @RequestParam String url) {
        String email = principal.getEmail();
        storeService.createStore(email, name, url);
        return "redirect:/stores";
    }

    @GetMapping("/{url}")
    public String viewStore(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url); // 実装要
        model.addAttribute("store", store);
        return "store/view"; // store/view.html を表示
    }
}

