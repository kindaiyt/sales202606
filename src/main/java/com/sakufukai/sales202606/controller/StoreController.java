package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.service.StoreService;
import com.sakufukai.sales202606.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/store")
public class StoreController {

    private final UserService userService;
    private final StoreService storeService;

    public StoreController(UserService userService, StoreService storeService) {
        this.userService = userService;
        this.storeService = storeService;
    }

    // 自分の店舗一覧
    @GetMapping
    public String myStores(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        // OAuth2 の認証情報にキャスト
        var oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
        var attributes = oauthToken.getPrincipal().getAttributes();

        // Google アカウントのメールアドレスを取得
        String email = (String) attributes.get("email");

        var user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));


        model.addAttribute("stores",
                user.getUserStores().stream()
                        .map(UserStore::getStore)
                        .toList()
        );
        return "store/list";
    }

    // 店舗詳細をURLで取得
    @GetMapping("/{url}")
    public String storeDetail(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url);  // Optionalではない
        if (store == null) {
            throw new IllegalArgumentException("店舗が見つかりません: " + url);
        }
        model.addAttribute("store", store);
        return "store/store";
    }
}

