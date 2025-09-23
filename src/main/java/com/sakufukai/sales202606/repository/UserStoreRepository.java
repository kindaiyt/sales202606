package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.UserStore;
import com.sakufukai.sales202606.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStoreRepository extends JpaRepository<UserStore, Long> {
    List<UserStore> findByUser(User user);
    List<UserStore> findByStore(Store store);
}
