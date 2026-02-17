package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.config.AppProperties;
import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.service.StoreService;
import com.sakufukai.sales202606.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final StoreService storeService;
    private final AppProperties appProperties;

    public AdminController(UserService userService,
                           StoreService storeService,
                           AppProperties appProperties) {
        this.userService = userService;
        this.storeService = storeService;
        this.appProperties = appProperties;
    }

    // ユーザー一覧ページ
    @GetMapping("/users")
    public String listUsers(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("stores", storeService.findAllWithUsers());
        model.addAttribute("fixedAdmins", appProperties.getAdminEmails());
        model.addAttribute("myEmail", oidcUser != null ? oidcUser.getAttribute("email") : null);
        return "admin/users"; // admin/users.html
    }

    // ユーザーロール変更
    @PostMapping("/users/{email}/role")
    public String changeRole(@AuthenticationPrincipal OidcUser oidcUser,
                             @PathVariable String email,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttributes) {
        String actorEmail = oidcUser.getAttribute("email");
        try {
            userService.changeUserRole(email, role, actorEmail);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

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

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("email", "");
        model.addAttribute("role", "USER");
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam(required = false) String email,
                             @RequestParam(required = false) String role,
                             Model model) {

        model.addAttribute("email", email);
        model.addAttribute("role", role);

        boolean hasError = false;

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("emailError", "メールアドレスを入力してください。");
            hasError = true;
        } else if (!email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            model.addAttribute("emailError", "メールアドレスの形式が正しくありません。");
            hasError = true;
        }

        Role roleEnum = Role.USER;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = Role.valueOf(role.trim());
            } catch (Exception e) {
                model.addAttribute("roleError", "ロールの値が不正です。");
                hasError = true;
            }
        }

        if (hasError) {
            return "admin/user-form";
        }

        try {
            userService.createUserWithPlaceholderName(email.trim(), roleEnum);
        } catch (IllegalArgumentException e) {
            model.addAttribute("emailError", e.getMessage());
            return "admin/user-form";
        }

        return "redirect:/admin/users";
    }

    // ユーザー削除
    @PostMapping("/users/{email}/delete")
    public String deleteUser(@AuthenticationPrincipal OidcUser oidcUser,
                             @PathVariable String email,
                             RedirectAttributes redirectAttributes) {
        String actorEmail = oidcUser.getAttribute("email");
        try {
            userService.deleteUserByEmail(email, actorEmail);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

}
