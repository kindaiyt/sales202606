package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.config.AppProperties;
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

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final UserStoreRepository userStoreRepository;
    private final AppProperties appProperties;

    public UserService(UserRepository userRepository,
                       StoreRepository storeRepository,
                       UserStoreRepository userStoreRepository,
                       AppProperties appProperties) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.userStoreRepository = userStoreRepository;
        this.appProperties = appProperties;
    }

    /**
     * メールアドレスでユーザーのロール変更
     */
    @Transactional
    public void changeUserRole(String targetEmail, Role newRole, String actorEmail) {

        if (isFixedAdmin(targetEmail)) {
            throw new IllegalArgumentException("この管理者アカウントのロールは変更できません。");
        }

        // 自分自身の ADMIN を USER に変更するのを禁止
        if (isSelf(targetEmail, actorEmail) && newRole == Role.USER) {
            throw new IllegalArgumentException("自分自身の管理者権限は外せません。");
        }

        User user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません。"));

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

    public String getNameByEmailOrGuest(String email) {
        if (email == null) return "ゲスト";
        return userRepository.findById(email)
                .map(User::getName)
                .orElse("ゲスト");
    }

    public boolean isAdminByEmail(String email) {
        if (email == null) return false;
        return userRepository.findById(email)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public User requireExistingUser(OidcUser oidcUser) {
        String email = oidcUser.getAttribute("email");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("NOT_REGISTERED"));
    }

    @Transactional
    public void createUserWithPlaceholderName(String email, Role role) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています。");
        });

        User user = new User();
        user.setEmail(email);
        user.setName("（未ログイン）");
        user.setRole(role == null ? Role.USER : role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Transactional
    public void deleteUserByEmail(String targetEmail, String actorEmail) {

        if (isFixedAdmin(targetEmail)) {
            throw new IllegalArgumentException("この管理者アカウントは削除できません。");
        }

        // 自分自身の削除を禁止
        if (isSelf(targetEmail, actorEmail)) {
            throw new IllegalArgumentException("自分自身は削除できません。");
        }

        User user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません。"));

        // 紐付けを先に削除（FK対策）
        userStoreRepository.deleteByUser(user);

        userRepository.delete(user);
    }

    private boolean isFixedAdmin(String email) {
        return appProperties.getAdminEmails() != null
                && appProperties.getAdminEmails().contains(email);
    }

    private boolean isSelf(String targetEmail, String actorEmail) {
        if (targetEmail == null || actorEmail == null) return false;
        return targetEmail.trim().equalsIgnoreCase(actorEmail.trim());
    }

    public List<User> findAllSorted() {
        return userRepository.findAllByOrderBySortOrderAscEmailAsc();
    }

    @Transactional
    public void updateSortOrder(String orderedIds) {
        if (orderedIds == null || orderedIds.trim().isEmpty()) return;

        String[] emails = orderedIds.split(",");
        int order = 1;

        for (String emailRaw : emails) {
            String email = emailRaw == null ? null : emailRaw.trim();
            if (email == null || email.isEmpty()) continue;

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) continue;

            user.setSortOrder(order);
            userRepository.save(user);

            order++;
        }
    }

}
