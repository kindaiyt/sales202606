package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    @Transactional
    public User loadOrCreateUser(OidcUser oidcUser) {
        String googleId = oidcUser.getSubject();
        String name = oidcUser.getFullName();

        // DBに存在するか確認
        return userRepository.findByGoogleId(googleId).orElseGet(() -> {
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setName(name);
            newUser.setRole("PENDING"); // デフォルトは承認待ち
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public void changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + userId));
        user.setRole(newRole);
        userRepository.save(user);
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }
}
