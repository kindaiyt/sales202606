package com.sakufukai.sales202606.controller;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    // 仮のマッピング（本番はDB管理が推奨）
    private static final Map<String, StoreInfo> storeMapping = new HashMap<>();
    static {
        storeMapping.put("2533340439b@kindai.ac.jp", new StoreInfo("Aliceのお店", "/store/alice", "admin-alice@example.com"));
        storeMapping.put("bob@gmail.com", new StoreInfo("Bobのショップ", "/store/bob", "admin-bob@example.com"));
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (oidcUser != null) {
            String email = oidcUser.getEmail();
            model.addAttribute("userName", oidcUser.getFullName());

            StoreInfo storeInfo = storeMapping.get(email);
            if (storeInfo != null) {
                model.addAttribute("storeName", storeInfo.getName());
                model.addAttribute("storeUrl", storeInfo.getUrl());
                model.addAttribute("adminEmail", storeInfo.getAdminEmail());
            }
        } else {
            model.addAttribute("userName", "ゲスト");
        }
        return "home"; // home.html
    }

    // 店舗情報を保持する内部クラス
    static class StoreInfo {
        private String name;
        private String url;
        private String adminEmail;

        public StoreInfo(String name, String url, String adminEmail) {
            this.name = name;
            this.url = url;
            this.adminEmail = adminEmail;
        }
        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getAdminEmail() { return adminEmail; }
    }
}
