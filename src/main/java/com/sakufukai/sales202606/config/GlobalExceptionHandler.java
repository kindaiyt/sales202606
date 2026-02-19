package com.sakufukai.sales202606.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 権限なし → 404 に寄せる
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "forward:/error/404";
    }

    /**
     * URLが存在しない・対象が見つからない系も 404 に寄せる（必要なら）
     * ※「入力エラー」まで 404 にしたくないなら、ここは限定してください。
     */
    @ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public String handleNotFoundLike(RuntimeException ex, HttpServletResponse response) {
        // “not found” 系を広めに 404 に寄せたい場合はこれでOK
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "forward:/error/404";
    }
}
