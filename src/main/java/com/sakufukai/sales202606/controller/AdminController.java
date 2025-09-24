package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.service.StoreService;
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
    private final StoreService storeService;

    // 複数の管理者メールを想定
    private static final List<String> ADMIN_EMAILS = List.of(
            "2533340439b@kindai.ac.jp",
            "another.admin@example.com"
    );

    public AdminController(UserService userService, StoreService storeService) {
        this.userService = userService;
        this.storeService = storeService;
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
        model.addAttribute("stores", storeService.findAll()); // ★ 店舗一覧を追加
        return "admin/users"; // admin/users.html
    }

    // ユーザーロール変更
    @PostMapping("/users/{email}/role")
    public String changeRole(@AuthenticationPrincipal OidcUser oidcUser,
                             @PathVariable String email,
                             @RequestParam Role role) {
        if (!isAdmin(oidcUser)) {
            return "redirect:/";
        }
        userService.changeUserRole(email, role);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{email}/store/add")
    public String addStoreToUser(@PathVariable String email, @RequestParam Long storeId) {
        userService.addStoreToUser(email, storeId);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{email}/store/{storeId}/remove")
    public String removeStoreFromUser(@PathVariable String email, @PathVariable Long storeId) {
        userService.removeStoreFromUser(email, storeId);
        return "redirect:/admin/users";
    }


}
