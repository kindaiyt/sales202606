package com.sakufukai.sales202606.service;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.repository.StoreRepository;
import com.sakufukai.sales202606.repository.UserRepository;
import com.sakufukai.sales202606.repository.UserStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void deleteStore(Long id) {
        storeRepository.deleteById(id);
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

}
