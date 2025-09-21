package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

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

        // DB からユーザー取得または新規作成
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(googleName != null ? googleName : "(未ログイン)");
            newUser.setRole(Role.PENDING);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        // 登録済みユーザーのうち、名前が「（未ログイン）」なら Google 名で更新
        if ((user.getName() == null || user.getName().contains("未ログイン")) && googleName != null) {
            user.setName(googleName);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // DB 上の名前で OidcUserInfo を作り直す
        OidcIdToken idToken = oidcUser.getIdToken();
        OidcUserInfo userInfo = new OidcUserInfo(
                Collections.singletonMap("name", user.getName())
        );

        String roleName = "ROLE_" + user.getRole().name();

        // DB 名を反映した OidcUser を返す
        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority(roleName)),
                idToken,
                userInfo,
                "name" // ここを name にすることで getName() が DB 名になる
        );
    }
}
