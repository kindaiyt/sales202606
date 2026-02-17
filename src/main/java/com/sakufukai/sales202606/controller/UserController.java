package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/me/edit")
    public String updateName(@AuthenticationPrincipal OidcUser oidcUser,
                             @RequestParam(required = false) String name,
                             Model model) {

        String email = oidcUser.getEmail();
        // Optional から User を取り出す
        User user = userService.findByEmail(oidcUser.getEmail())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        // 入力値保持
        model.addAttribute("user", user);
        model.addAttribute("name", name);

        // ★ 空白チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "名前を入力してください。");
            return "users/edit";   // ← edit.html のパスに合わせる
        }

        userService.updateUserInfo(email, name.trim());

        return "redirect:/users/me";
    }

}

