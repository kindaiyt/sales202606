package com.sakufukai.sales202606.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全コントローラ共通でモデルに値を追加するクラス
 */
@ControllerAdvice
public class GlobalModelAttributes {

    /**
     * 各リクエストのURIをモデルに追加する
     * @param request HttpServletRequest
     * @return 現在のリクエストURI
     */
    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
