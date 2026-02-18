package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.service.StoreService;
import com.sakufukai.sales202606.service.UserService;
import com.sakufukai.sales202606.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/store")
public class StoreController {

    private final UserService userService;
    private final StoreService storeService;
    private final ProductService productService;

    public StoreController(UserService userService,
                           StoreService storeService,
                           ProductService productService) {
        this.userService = userService;
        this.storeService = storeService;
        this.productService = productService;
    }

    // 自分の店舗一覧
    @GetMapping
    public String myStores(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        // OAuth2 の認証情報にキャスト
        var oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
        var attributes = oauthToken.getPrincipal().getAttributes();

        // Google アカウントのメールアドレスを取得
        String email = (String) attributes.get("email");

        var user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));


        model.addAttribute("stores",
                user.getUserStores().stream()
                        .map(UserStore::getStore)
                        .toList()
        );
        return "store/list";
    }

    // 店舗詳細をURLで取得
    @GetMapping("/{url}")
    public String storeDetail(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url);  // Optionalではない
        if (store == null) {
            throw new IllegalArgumentException("店舗が見つかりません: " + url);
        }
        model.addAttribute("store", store);
        // ★追加: リンク化した備考を作る
        model.addAttribute("noteWithLinks",
                convertUrlsToLinks(store.getNote()));
        List<Product> products = productService.findByStoreSorted(store);
        model.addAttribute("products", products);
        // ★追加: 並べ替えボタン表示用
        model.addAttribute("hasProducts", productService.countByStore(store) > 1);
        return "store/store";
    }

    private String convertUrlsToLinks(String text) {
        if (text == null) return "";

        return text
                .replaceAll(
                        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
                        "<a href=\"$1\" target=\"_blank\">$1</a>"
                )
                .replace("\n", "<br>");
    }

    @PostMapping("/{url}/note")
    public String updateStoreNote(@PathVariable String url,
                                  @RequestParam(required = false) String note,
                                  Authentication authentication) {

        storeService.updateStoreNote(url, note, authentication);
        return "redirect:/store/" + url;
    }

    @GetMapping("/{url}/note/edit")
    public String editStoreNotePage(@PathVariable String url, Model model) {
        Store store = storeService.findByUrl(url);
        model.addAttribute("store", store);
        return "store/store-note"; // templates/store/store-note.html
    }

}

