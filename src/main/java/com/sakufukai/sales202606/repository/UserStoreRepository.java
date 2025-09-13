package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.UserStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStoreRepository extends JpaRepository<UserStore, Long> {
    List<UserStore> findByUserId(Long userId);
    List<UserStore> findByStoreId(Long storeId);
}
