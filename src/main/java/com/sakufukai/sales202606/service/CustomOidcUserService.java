package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 標準の OidcUser を取得
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getAttribute("email");
        String googleName = oidcUser.getAttribute("name");

        // email が取れない場合はログイン失敗にする
        if (email == null || email.trim().isEmpty()) {
            OAuth2Error error = new OAuth2Error("invalid_user", "Email not found from OIDC provider", null);
            throw new OAuth2AuthenticationException(error);
        }

        // DB に存在しないユーザーは作らない: ログイン失敗にする
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    OAuth2Error error = new OAuth2Error("not_registered", "User is not registered", null);
                    return new OAuth2AuthenticationException(error);
                });

        // 既存ユーザーの名前が空っぽ系なら Google 名で更新（任意）
        if ((user.getName() == null || user.getName().trim().isEmpty() || user.getName().contains("未ログイン"))
                && googleName != null && !googleName.trim().isEmpty()) {
            user.setName(googleName.trim());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // userInfo に email と name を入れて返す: 他の画面で oidcUser.getEmail() が壊れにくい
        OidcIdToken idToken = oidcUser.getIdToken();

        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("email", email);
        userInfoMap.put("name", user.getName());

        OidcUserInfo userInfo = new OidcUserInfo(userInfoMap);

        String roleName = "ROLE_" + user.getRole().name();

        return new DefaultOidcUser(
                Set.of(new SimpleGrantedAuthority(roleName)),
                idToken,
                userInfo,
                "name"
        );
    }
}
