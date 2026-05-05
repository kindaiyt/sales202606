package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.StoreType;
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
        List<User> users = userService.findAllSorted();

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
            List<User> users = userService.findAllSorted();

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
                                  @RequestParam(required = false) String locationName,
                                  @RequestParam(required = false) String storeType,
                                  Model model) {

        Store store = storeService.findByUrl(url);
        model.addAttribute("store", store);

        // 入力値保持
        model.addAttribute("name", name);
        model.addAttribute("newUrl", newUrl);
        model.addAttribute("locationName", locationName);
        model.addAttribute("storeType", storeType);

        boolean hasError = false;

        // 店舗名チェック
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "店舗名を入力してください。");
            hasError = true;
        }

        // URLチェック（重複チェックは StoreService 側で実装）
        String trimmedNewUrl = (newUrl == null) ? null : newUrl.trim();
        if (trimmedNewUrl == null || trimmedNewUrl.isEmpty()) {
            model.addAttribute("urlError", "店舗URLを入力してください。");
            hasError = true;
        }

        // 店舗種別チェック
        StoreType type = null;
        if (storeType == null || storeType.isBlank()) {
            model.addAttribute("storeTypeError", "店舗種別を選択してください。");
            hasError = true;
        } else {
            try {
                type = StoreType.valueOf(storeType);
            } catch (Exception e) {
                model.addAttribute("storeTypeError", "不正な店舗種別です。");
                hasError = true;
            }
        }

        if (hasError) {
            return "admin/store-info-edit";
        }

        try {
            storeService.updateStoreInfo(url, name, trimmedNewUrl, locationName, storeType);
        } catch (IllegalArgumentException e) {
            model.addAttribute("urlError", e.getMessage());
            return "admin/store-info-edit";
        }

        return "redirect:/admin/stores/" + trimmedNewUrl + "/edit";
    }

}
