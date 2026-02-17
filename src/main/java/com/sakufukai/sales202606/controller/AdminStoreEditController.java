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
public class AdminStoreEditController {

    private final StoreService storeService;
    private final UserService userService;

    @GetMapping("/admin/stores/{url}/edit")
    public String editPage(@PathVariable String url, Model model) {
        Store store = storeService.findByUrlWithUsers(url);
        List<User> users = userService.findAllApprovedUsers();

        model.addAttribute("store", store);
        model.addAttribute("users", users);

        return "admin/store-edit";
    }

    @PostMapping("/admin/stores/{url}/edit/add")
    public String add(@PathVariable String url,
                      @RequestParam(required = false) String userEmail,
                      Model model) {

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Store store = storeService.findByUrlWithUsers(url);
            List<User> users = userService.findAllApprovedUsers();

            model.addAttribute("store", store);
            model.addAttribute("users", users);

            model.addAttribute("userError", "追加するユーザーを選択してください。");
            model.addAttribute("selectedUserEmail", userEmail);

            return "admin/store-edit";
        }

        storeService.addUserToStoreByUrl(url, userEmail);
        return "redirect:/admin/stores/" + url + "/edit";
    }

    @PostMapping("/admin/stores/{url}/edit/remove")
    public String remove(@PathVariable String url,
                         @RequestParam String userEmail) {
        storeService.removeUserFromStoreByUrl(url, userEmail);
        return "redirect:/admin/stores/" + url + "/edit";
    }

    @GetMapping("/admin/stores/{url}/edit/info")
    public String editStoreInfoPage(@PathVariable String url, Model model) {
        Store store = storeService.findByUrlWithUsers(url); // なければ findByUrl でもOK
        model.addAttribute("store", store);
        return "admin/store-info-edit";
    }

    @PostMapping("/admin/stores/{url}/edit/info")
    public String updateStoreInfo(@PathVariable String url,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) String newUrl,
                                  Model model) {

        Store store = storeService.findByUrl(url);

        // 入力値保持
        model.addAttribute("store", store);
        model.addAttribute("name", name);
        model.addAttribute("newUrl", newUrl);

        boolean hasError = false;

        // 店舗名チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "店舗名を入力してください。");
            hasError = true;
        }

        // URLチェック
        if (newUrl == null || newUrl.trim().isEmpty()) {
            model.addAttribute("urlError", "店舗URLを入力してください。");
            hasError = true;
        }

        if (hasError) {
            return "admin/store-info-edit";
        }

        try {
            storeService.updateStoreInfo(url, name, newUrl);
        } catch (IllegalArgumentException e) {
            model.addAttribute("urlError", e.getMessage());
            return "admin/store-info-edit";
        }

        return "redirect:/admin/stores/" + newUrl + "/edit";
    }

}
