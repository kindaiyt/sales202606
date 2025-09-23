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
        return storeRepository.save(store);
    }
}
