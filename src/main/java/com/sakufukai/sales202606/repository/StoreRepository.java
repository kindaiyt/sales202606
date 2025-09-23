package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByUrl(String url);
}
