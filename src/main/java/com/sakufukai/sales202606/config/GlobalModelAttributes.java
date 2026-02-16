package com.sakufukai.sales202606.config;

import com.sakufukai.sales202606.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserService userService;

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("userEmail")
    public String userEmail(Authentication authentication) {
        return extractEmail(authentication);
    }

    @ModelAttribute("userName")
    public String userName(Authentication authentication) {
        String email = extractEmail(authentication);
        return (email == null) ? "ゲスト" : userService.getNameByEmailOrGuest(email);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        String email = extractEmail(authentication);
        return (email != null) && userService.isAdminByEmail(email);
    }

    private String extractEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            Object emailObj = oauth2User.getAttribute("email");
            return (emailObj != null) ? emailObj.toString() : null;
        }
        return null;
    }
}
