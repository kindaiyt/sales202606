package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.*;
import com.sakufukai.sales202606.repository.StoreRepository;
import com.sakufukai.sales202606.repository.UserRepository;
import com.sakufukai.sales202606.repository.UserStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

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

        // ★ここだけに統一（ADMIN or 所属）
        assertMemberOrAdmin(store, authentication);

        store.setNote(note); // 空欄OK（null/""でもOK）
        storeRepository.save(store);
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
    public void updateStoreInfo(String currentUrl,
                                String name,
                                String newUrl,
                                String storeType) {
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
        store.setStoreType(StoreType.valueOf(storeType));
        storeRepository.save(store);
    }

    public boolean existsByUrl(String url) {
        return storeRepository.existsByUrl(url);
    }

    /**
     * 認証情報から email を取り出す（他サービスからも使えるよう public）
     */
    public String extractEmail(Authentication authentication) {
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

    /**
     * ADMIN かどうか
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(String email) {
        if (email == null || email.isBlank()) return false;
        return userRepository.findByEmail(email)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    /**
     * 店舗に所属しているか（UserStore があるか）
     */
    @Transactional(readOnly = true)
    public boolean isAssignedToStore(String email, Store store) {
        if (email == null || email.isBlank() || store == null) return false;
        return userRepository.findByEmail(email)
                .map(u -> userStoreRepository.existsByUserAndStore(u, store))
                .orElse(false);
    }

    /**
     * 店舗にアクセス/操作できるか（ADMIN または 所属）
     */
    @Transactional(readOnly = true)
    public boolean canAccessStore(Store store, Authentication authentication) {
        String email = extractEmail(authentication);
        if (email == null) return false;
        return isAdmin(email) || isAssignedToStore(email, store);
    }

    /**
     * 失敗したら AccessDeniedException を投げる（Controller 側で 404 に寄せる用途）
     */
    @Transactional(readOnly = true)
    public void assertMemberOrAdmin(Store store, Authentication authentication) {
        if (!canAccessStore(store, authentication)) {
            throw new AccessDeniedException("権限がありません");
        }
    }

    /**
     * storeId 版（/product/edit/{id} のように URL が無いケースで使う）
     */
    @Transactional(readOnly = true)
    public void assertMemberOrAdmin(Long storeId, Authentication authentication) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("店舗が見つかりません: " + storeId));
        assertMemberOrAdmin(store, authentication);
    }

    /**
     * url 版：店舗取得 + 権限チェック + 返却（Controller が一行になる）
     */
    @Transactional(readOnly = true)
    public Store getStoreForMemberOrAdmin(String url, Authentication authentication) {
        Store store = findByUrl(url); // 既存メソッドを利用（見つからなければ例外）
        assertMemberOrAdmin(store, authentication);
        return store;
    }

    @Transactional(readOnly = true)
    public List<Store> findByType(StoreType storeType) {
        return storeRepository.findByStoreType(storeType);
    }

    public List<Store> sortStores(List<Store> stores, String sort, String dir) {
        if (stores == null) return List.of();
        if (!"name".equals(sort)) return stores;

        boolean desc = "desc".equalsIgnoreCase(dir);

        return stores.stream()
                .sorted((a, b) -> {
                    String an = a.getName() == null ? "" : a.getName();
                    String bn = b.getName() == null ? "" : b.getName();
                    int cmp = an.compareToIgnoreCase(bn);
                    return desc ? -cmp : cmp;
                })
                .toList();
    }

}
