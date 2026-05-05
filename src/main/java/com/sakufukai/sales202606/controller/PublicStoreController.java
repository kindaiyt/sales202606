package com.sakufukai.sales202606.controller;

import com.sakufukai.sales202606.entity.Product;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.StoreType;
import com.sakufukai.sales202606.service.ProductService;
import com.sakufukai.sales202606.service.StoreService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/stores")
public class PublicStoreController {

    private final StoreService storeService;
    private final ProductService productService;

    public PublicStoreController(StoreService storeService,
                                  ProductService productService) {
        this.storeService = storeService;
        this.productService = productService;
    }

    @GetMapping
    public String storesTop() {
        return "public/stores-top";
    }

    // 学生の模擬店舗用
    @GetMapping("/student")
    public String listStudentStores(Model model, HttpSession session) {

        List<Store> stores = storeService.findByType(StoreType.STUDENT);

        String sort = (String) session.getAttribute("publicStoreSort");
        String dir  = (String) session.getAttribute("publicStoreDir");

        stores = storeService.sortStores(stores, sort, dir);

        model.addAttribute("stores", stores);
        model.addAttribute("hasStores", stores != null && !stores.isEmpty());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("title", "学生の模擬店舗一覧"); // ★追加

        model.addAttribute("storeType", "student");

        return "public/stores";
    }

    // 一般店舗用
    @GetMapping("/general")
    public String listGeneralStores(Model model, HttpSession session) {

        List<Store> stores = storeService.findByType(StoreType.GENERAL);

        String sort = (String) session.getAttribute("publicStoreSort");
        String dir  = (String) session.getAttribute("publicStoreDir");

        stores = storeService.sortStores(stores, sort, dir);

        model.addAttribute("stores", stores);
        model.addAttribute("hasStores", stores != null && !stores.isEmpty());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("title", "一般店舗一覧"); // ★追加

        model.addAttribute("storeType", "general");

        return "public/stores";
    }

    @PostMapping("/view-sort")
    public String sortStoreList(@RequestParam(required = false) String sort,
                                @RequestParam(required = false) String dir,
                                @RequestParam(required = false) String redirectTo,
                                HttpSession session) {

        // 防御：一般公開は name のみ
        if (!"name".equals(sort)) sort = null;
        dir = "desc".equalsIgnoreCase(dir) ? "desc" : "asc";

        if (sort == null) {
            session.removeAttribute("publicStoreSort");
            session.removeAttribute("publicStoreDir");
        } else {
            session.setAttribute("publicStoreSort", sort);
            session.setAttribute("publicStoreDir", dir);
        }

        // 学生の模擬店舗および一般店舗用のリダイレクト
        if ("/stores/student".equals(redirectTo) || "/stores/general".equals(redirectTo)) {
            return "redirect:" + redirectTo;
        }

        return "redirect:/stores";
    }

    @PostMapping("/view-sort/clear")
    public String clearStoreListSort(@RequestParam(required = false) String redirectTo,
                                     HttpSession session) {
        session.removeAttribute("publicStoreSort");
        session.removeAttribute("publicStoreDir");

        // 学生の模擬店舗および一般店舗用のリダイレクト
        if ("/stores/student".equals(redirectTo) || "/stores/general".equals(redirectTo)) {
            return "redirect:" + redirectTo;
        }

        return "redirect:/stores";
    }

    // =========================
    // 一般公開：店舗詳細
    // ・店舗説明(note)はリンク化して表示
    // ・商品は「登録順(sortOrder)」がベース
    // ・表示用ソート：商品名/価格（DB順は変えない）
    // ・ソート状態は Session に保持（URLは /stores/{url} のまま）
    // =========================
    @GetMapping("/{storeType}/{url}")
    public String storeDetail(@PathVariable String storeType,
                              @PathVariable String url,
                              Model model,
                              HttpSession session) {

        StoreType expectedType;

        if ("student".equals(storeType)) {
            expectedType = StoreType.STUDENT;
        } else if ("general".equals(storeType)) {
            expectedType = StoreType.GENERAL;
        } else {
            return "error/404";
        }

        Store store;
        try {
            store = storeService.findByUrl(url);
        } catch (RuntimeException e) {
            return "error/404";
        }

        if (store.getStoreType() != expectedType) {
            return "error/404";
        }

        String sort = (String) session.getAttribute("publicProductSort"); // "name"/"price"
        String dir  = (String) session.getAttribute("publicProductDir");  // "asc"/"desc"

        // ★ここが重要：あなたの ProductService に既にあるメソッドを使う
        List<Product> products = productService.findByStoreSorted(store, sort, dir);

        model.addAttribute("store", store);
        model.addAttribute("noteWithLinks", convertUrlsToLinks(store.getNote()));

        model.addAttribute("products", products);
        model.addAttribute("hasProducts", products != null && !products.isEmpty());

        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        model.addAttribute("storeType", storeType);

        return "public/store";
    }

    @PostMapping("/{storeType}/{url}/view-sort")
    public String sortProducts(@PathVariable String storeType,
                               @PathVariable String url,
                               @RequestParam(required = false) String sort,
                               @RequestParam(required = false) String dir,
                               HttpSession session) {

        // 防御：name/price/soldOut 以外は無効化
        if (!"name".equals(sort) && !"price".equals(sort) && !"soldOut".equals(sort)) sort = null;
        dir = "desc".equalsIgnoreCase(dir) ? "desc" : "asc";

        if (sort == null) {
            session.removeAttribute("publicProductSort");
            session.removeAttribute("publicProductDir");
        } else {
            session.setAttribute("publicProductSort", sort);
            session.setAttribute("publicProductDir", dir);
        }

        // ★URLは常に /stores/{url} に戻す
        return "redirect:/stores/" + storeType + "/" + url;
    }

    @PostMapping("/{storeType}/{url}/view-sort/clear")
    public String clearProductSort(@PathVariable String storeType,
                                   @PathVariable String url,
                                   HttpSession session) {
        session.removeAttribute("publicProductSort");
        session.removeAttribute("publicProductDir");
        return "redirect:/stores/" + storeType + "/" + url;
    }

    // URLリンク化 + 改行→<br>
    private String convertUrlsToLinks(String text) {
        if (text == null) return "";
        return text
                .replaceAll(
                        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
                        "<a href=\"$1\" target=\"_blank\" rel=\"noopener noreferrer\">$1</a>"
                )
                .replace("\n", "<br>");
    }
}
