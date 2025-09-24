package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.repository.StoreRepository;
import com.sakufukai.sales202606.repository.UserRepository;
import com.sakufukai.sales202606.repository.UserStoreRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final UserStoreRepository userStoreRepository;

    public UserService(UserRepository userRepository,
                       StoreRepository storeRepository,
                       UserStoreRepository userStoreRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.userStoreRepository = userStoreRepository;
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

    @Transactional
    public void addStoreToUser(String email, Long storeId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // 既に紐付け済みなら何もしない
        boolean alreadyLinked = userStoreRepository.existsByUserAndStore(user, store);
        if (!alreadyLinked) {
            UserStore userStore = new UserStore();
            userStore.setUser(user);
            userStore.setStore(store);
            userStoreRepository.save(userStore);
        }
    }

    @Transactional
    public void removeStoreFromUser(String email, Long storeId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        userStoreRepository.findByUserAndStore(user, store).ifPresent(userStoreRepository::delete);
    }


}
