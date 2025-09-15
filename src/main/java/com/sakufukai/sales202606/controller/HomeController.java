package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (oidcUser == null) {
            model.addAttribute("userName", "ゲスト");
            return "home";
        }

        // DBからユーザーを取得（初回は新規登録 → role=PENDING）
        User user = userService.loadOrCreateUser(oidcUser);

        // 承認待ちなら専用画面へ
        if (user.getRole() == Role.PENDING) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("message", "管理者の承認待ちです。");
            return "pending"; // pending.html を用意
        }

        model.addAttribute("userName", user.getName());

        // 管理者の場合のフラグ
        boolean isAdmin = user.getRole() == Role.ADMIN;
        model.addAttribute("isAdmin", isAdmin);

        // ユーザーが持つ店舗を取得（仮に1店舗目を使用）
        List<UserStore> userStores = user.getUserStores();
        if (userStores != null && !userStores.isEmpty()) {
            Store store = userStores.get(0).getStore();
            model.addAttribute("storeName", store.getName());
            model.addAttribute("storeUrl", "/store/" + store.getName());
        } else {
            model.addAttribute("storeName", "店舗が登録されていません");
        }

        return "home"; // home.html
    }
}
