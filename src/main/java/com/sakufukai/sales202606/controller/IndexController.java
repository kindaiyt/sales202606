package com.sakufukai.sales202606.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class IndexController {

    @GetMapping("/index")
    public String index(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("loggedIn", true);
        } else {
            model.addAttribute("userName", "ゲスト");
            model.addAttribute("loggedIn", false);
        }
        return "index";
    }
}
