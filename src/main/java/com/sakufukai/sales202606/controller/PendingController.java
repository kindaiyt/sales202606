package com.sakufukai.sales202606.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PendingController {

    @GetMapping("/pending")
    public String pending(@RequestParam(required = false) String reason, Model model) {

        if ("not_registered".equals(reason)) {
            model.addAttribute("userName", "ゲスト");
            model.addAttribute("message", "このGoogleアカウントは店舗管理者として登録されていません。");
            return "pending";
        }

        if ("invalid_user".equals(reason)) {
            model.addAttribute("userName", "ゲスト");
            model.addAttribute("message", "Googleアカウント情報を取得できませんでした。別のアカウントでお試しください。");
            return "pending";
        }

        // それ以外（一般的なログイン失敗など）
        model.addAttribute("userName", "ゲスト");
        model.addAttribute("message", "ログインに失敗しました。もう一度お試しください。");
        return "pending";
    }
}
