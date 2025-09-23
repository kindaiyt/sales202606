package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 一般ユーザー用: 自分の情報確認ページ
    @GetMapping("/me")
    public String myPage(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (oidcUser == null) {
            return "redirect:/login";
        }
        var user = userService.findByEmail(oidcUser.getEmail());
        System.out.println("DEBUG: user = " + user); // ログ出力
        model.addAttribute("user", userService.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found")));
        return "users/me"; // users/me.html
    }

    @GetMapping("/me/edit")
    public String editForm(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (oidcUser == null) {
            return "redirect:/login";
        }

        // Optional から User を取り出す
        User user = userService.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        model.addAttribute("user", user);
        return "users/edit"; // users/edit.html
    }


    // 更新処理
    @PostMapping("/me/edit")
    public String updateUser(@AuthenticationPrincipal OidcUser oidcUser,
                             @ModelAttribute("user") User formUser) {
        if (oidcUser == null) {
            return "redirect:/login";
        }
        userService.updateUserInfo(oidcUser.getEmail(), formUser.getName());
        return "redirect:/users/me"; // 更新後マイページへ
    }

}

