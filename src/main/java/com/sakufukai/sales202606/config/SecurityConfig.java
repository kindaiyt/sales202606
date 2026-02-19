package com.sakufukai.sales202606.config;

import com.sakufukai.sales202606.service.CustomOidcUserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository,
                                                   CustomOidcUserService customOAuth2UserService) throws Exception {

        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        OAuth2AuthorizationRequestResolver resolver = new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return customize(defaultResolver.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return customize(defaultResolver.resolve(request, clientRegistrationId));
            }
        };

        http
                .authorizeHttpRequests(auth -> auth

                        // ===== 公開ページ（誰でもOK）=====
                        .requestMatchers("/", "/pending", "/error/**",
                                "/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/stores/**").permitAll() // 一般公開

                        // ===== ADMIN専用 =====
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ===== USER または ADMIN =====
                        .requestMatchers("/store/**", "/product/**", "/users/**")
                        .hasAnyRole("USER", "ADMIN")

                        // ===== その他は拒否 =====
                        .anyRequest().denyAll()
                )

                // ★追加: 権限不足(403)を 404専用ページへ
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            request.getRequestDispatcher("/error/404").forward(request, response);
                        })
                )

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(resolver)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .failureHandler((request, response, exception) -> {
                            // 未登録なら pending へ (クエリで理由を渡す)
                            String msg = "login_failed";

                            
                            if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException ex) {
                                String code = ex.getError().getErrorCode();
                                if ("not_registered".equals(code)) {
                                    msg = "not_registered";
                                } else if ("invalid_user".equals(code)) {
                                    msg = "invalid_user";
                                }
                            }

                            response.sendRedirect("/pending?reason=" + msg);
                        })
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                );


        return http.build();
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
        if (req == null) return null;
        return OAuth2AuthorizationRequest.from(req)
                .additionalParameters(params -> params.put("prompt", "login"))
                .build();
    }
}
