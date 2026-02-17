package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.repository.StoreRepository;
import com.sakufukai.sales202606.repository.UserRepository;
import com.sakufukai.sales202606.repository.UserStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserStoreRepository userStoreRepository;

    public StoreService(StoreRepository storeRepository,
                        UserRepository userRepository,
                        UserStoreRepository userStoreRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.userStoreRepository = userStoreRepository;
    }

    // 店舗作成
    public Store createStore(String email, String storeName, String url) {
        // ユーザーを取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 店舗を作成
        Store store = new Store();
        store.setName(storeName);
        store.setUrl(url);
        Store savedStore = storeRepository.save(store);

        // 中間テーブルで紐付け
        UserStore userStore = new UserStore();
        userStore.setUser(user);
        userStore.setStore(savedStore);
        userStoreRepository.save(userStore);

        return savedStore;
    }

    // 店舗をURLで検索
    public Store findByUrl(String url) {
        return storeRepository.findByUrl(url)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }

    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    @Transactional
    public Store save(Store store) {
        // 新規作成時のみチェック（id が null の場合）
        if (store.getId() == null) {
            storeRepository.findByUrl(store.getUrl()).ifPresent(existing -> {
                throw new IllegalArgumentException("このURLは既に存在します: " + store.getUrl());
            });
        }
        return storeRepository.save(store);
    }

    @Transactional
    public void deleteStoreByUrl(String url) {
        Store store = storeRepository.findByUrl(url)
                .orElseThrow(() -> new IllegalArgumentException("店舗が見つかりません。"));
        storeRepository.delete(store);
    }


    @Transactional(readOnly = true)
    public Store findByIdWithUsers(Long storeId) {
        return storeRepository.findWithUsersById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }

    @Transactional
    public void addUserToStore(Long storeId, String userEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // User の主キーは email なので findById が最短（findByEmailでもOK）
        User user = userRepository.findById(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 二重登録防止
        if (userStoreRepository.existsByUserAndStore(user, store)) {
            return;
        }

        UserStore userStore = new UserStore();
        userStore.setUser(user);
        userStore.setStore(store);
        userStoreRepository.save(userStore);
    }

    @Transactional
    public void removeUserFromStore(Long storeId, String userEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        User user = userRepository.findById(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userStoreRepository.deleteByUserAndStore(user, store);
    }

    @Transactional
    public void updateStoreNote(String url, String note, Authentication authentication) {
        Store store = findByUrl(url);

        // ログインユーザーのemail取得（Google OAuth）
        String email = extractEmail(authentication);
        if (email == null) throw new RuntimeException("email not found");

        // ADMINならOK
        boolean isAdmin = userRepository.findByEmail(email)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);

        // 店舗に割り当て済みならOK
        boolean assigned = userRepository.findByEmail(email)
                .map(u -> userStoreRepository.existsByUserAndStore(u, store))
                .orElse(false);

        if (!isAdmin && !assigned) {
            throw new org.springframework.security.access.AccessDeniedException("権限がありません");
        }

        store.setNote(note); // 空欄OK（null/""でもOK）
        storeRepository.save(store);
    }

    private String extractEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }
        if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            return oidcUser.getAttribute("email");
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Store> findAllWithUsers() {
        return storeRepository.findAllWithUsers();
    }

    public Store findByUrlWithUsers(String url) {
        return storeRepository.findByUrlWithUsers(url)
                .orElseThrow(() -> new IllegalArgumentException("店舗が存在しません: " + url));
    }

    public void addUserToStoreByUrl(String url, String userEmail) {
        Store store = findByUrlWithUsers(url);
        addUserToStore(store.getId(), userEmail);
    }

    public void removeUserFromStoreByUrl(String url, String userEmail) {
        Store store = findByUrlWithUsers(url);
        removeUserFromStore(store.getId(), userEmail);
    }

    @Transactional
    public void updateStoreInfo(String currentUrl, String name, String newUrl) {
        Store store = storeRepository.findByUrl(currentUrl)
                .orElseThrow(() -> new IllegalArgumentException("店舗が見つかりません: " + currentUrl));

        String trimmedName = name.trim();
        String trimmedUrl = newUrl.trim();

        // URLが変更される場合のみ重複チェック
        if (!store.getUrl().equals(trimmedUrl)) {
            storeRepository.findByUrl(trimmedUrl).ifPresent(existing -> {
                throw new IllegalArgumentException("このURLは既に存在します: " + trimmedUrl);
            });
            store.setUrl(trimmedUrl);
        }

        store.setName(trimmedName);
        storeRepository.save(store);
    }

}
