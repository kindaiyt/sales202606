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

        var oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
        var attributes = oauthToken.getPrincipal().getAttributes();
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

    // 店舗詳細
    @GetMapping("/{url}")
    public String storeDetail(@PathVariable String url,
                              Model model,
                              jakarta.servlet.http.HttpSession session) {

        Store store = storeService.findByUrl(url);
        if (store == null) throw new IllegalArgumentException("店舗が見つかりません: " + url);

        // セッションから並び替え条件を取得（なければnull）
        String key = "productSort:" + url;
        SortState state = (SortState) session.getAttribute(key);

        String sort = state != null ? state.sort() : null;
        String dir  = state != null ? state.dir()  : null;

        model.addAttribute("store", store);
        model.addAttribute("noteWithLinks", convertUrlsToLinks(store.getNote()));

        model.addAttribute("products", productService.findByStoreSorted(store, sort, dir));

        // ヘッダの▲▼用
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

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
        return "store/store-note";
    }

    @PostMapping("/{url}/view-sort")
    public String setViewSort(@PathVariable String url,
                              @RequestParam String sort,
                              @RequestParam String dir,
                              jakarta.servlet.http.HttpSession session) {

        String key = "productSort:" + url;
        session.setAttribute(key, new SortState(sort, dir));
        return "redirect:/store/" + url; // ★URLを綺麗に保つ
    }

    @PostMapping("/{url}/view-sort/clear")
    public String clearViewSort(@PathVariable String url,
                                jakarta.servlet.http.HttpSession session) {
        session.removeAttribute("productSort:" + url);
        return "redirect:/store/" + url;
    }

    // ここはStoreController内でOK（Java16未満なら普通のstatic classで）
    public record SortState(String sort, String dir) {}

}
