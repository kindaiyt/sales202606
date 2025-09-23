package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * メールアドレスをキーとしてユーザーを取得、なければ作成
     */
    @Transactional
    public User loadOrCreateUser(OidcUser oidcUser) {
        String name = oidcUser.getFullName();
        String email = oidcUser.getEmail();

        // メールアドレスで存在チェック
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole(Role.PENDING); // デフォルトは承認待ち
            return userRepository.save(newUser);
        });
    }

    /**
     * メールアドレスでユーザーのロール変更
     */
    @Transactional
    public void changeUserRole(String email, Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + email));
        user.setRole(newRole);
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateUserInfo(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + email));

        user.setName(name);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }

}
