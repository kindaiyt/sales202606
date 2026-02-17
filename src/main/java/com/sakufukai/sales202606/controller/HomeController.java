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

        User user;
        try {
            user = userService.requireExistingUser(oidcUser);
        } catch (IllegalStateException e) {
            model.addAttribute("userName", "ゲスト");
            model.addAttribute("message", "このGoogleアカウントは登録されていません。管理者に連絡してください。");
            return "pending"; // 既存の pending.html を流用でもOK（画面名は自由）
        }

        if (user.getRole() == Role.PENDING) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("message", "管理者の承認待ちです。");
            return "pending";
        }

        model.addAttribute("userName", user.getName());
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);

        List<UserStore> userStores = user.getUserStores();
        if (userStores != null && !userStores.isEmpty()) {
            Store store = userStores.get(0).getStore();
            model.addAttribute("storeName", store.getName());
            model.addAttribute("storeUrl", "/store/" + store.getUrl());
        }

        return "home";
    }

}
