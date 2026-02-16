package com.sakufukai.sales202606.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error/404")
    public String notFound() {
        return "error/404";
    }
}