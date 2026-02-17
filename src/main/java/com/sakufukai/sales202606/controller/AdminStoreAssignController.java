package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.service.StoreService;
import com.sakufukai.sales202606.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminStoreAssignController {

    private final StoreService storeService;
    private final UserService userService;

    @GetMapping("/admin/stores/{storeId}/assign")
    public String assignPage(@PathVariable Long storeId, Model model) {
        Store store = storeService.findByIdWithUsers(storeId); // userStores+userを取れると理想
        List<User> users = userService.findAllApprovedUsers(); // PENDING除外推奨

        model.addAttribute("store", store);
        model.addAttribute("users", users);
        return "admin/store-assign";
    }

    @PostMapping("/admin/stores/{storeId}/assign/add")
    public String add(@PathVariable Long storeId,
                      @RequestParam(required = false) String userEmail,
                      Model model) {

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Store store = storeService.findByIdWithUsers(storeId);
            List<User> users = userService.findAllApprovedUsers();

            model.addAttribute("store", store);
            model.addAttribute("users", users);

            model.addAttribute("userError", "追加するユーザーを選択してください。");
            model.addAttribute("selectedUserEmail", userEmail);

            return "admin/store-assign";
        }

        storeService.addUserToStore(storeId, userEmail);
        return "redirect:/admin/stores/" + storeId + "/assign";
    }

}

