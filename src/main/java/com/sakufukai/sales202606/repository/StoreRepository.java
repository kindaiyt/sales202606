package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    // 名前で検索したい場合
    Store findByName(String name);
}
