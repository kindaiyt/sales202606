package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    // 複数の管理者メールを想定
    private static final List<String> ADMIN_EMAILS = List.of(
            "2533340439b@kindai.ac.jp",
            "another.admin@example.com"
    );

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // 管理者チェック
    private boolean isAdmin(OidcUser oidcUser) {
        return oidcUser != null && ADMIN_EMAILS.contains(oidcUser.getEmail());
    }

    // ユーザー一覧ページ
    @GetMapping("/users")
    public String listUsers(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (!isAdmin(oidcUser)) {
            return "redirect:/";
        }
        model.addAttribute("users", userService.findAll());
        return "admin/users"; // admin/users.html
    }

    // ユーザーロール変更
    @PostMapping("/users/{id}/role")
    public String changeRole(@AuthenticationPrincipal OidcUser oidcUser,
                             @PathVariable Long id,
                             @RequestParam Role role) {
        if (!isAdmin(oidcUser)) {
            return "redirect:/";
        }
        userService.changeUserRole(id, role);
        return "redirect:/admin/users";
    }
}
