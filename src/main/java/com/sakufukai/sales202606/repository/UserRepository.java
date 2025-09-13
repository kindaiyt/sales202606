package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 追加で検索メソッドを定義することも可能
    User findByGoogleId(String googleId);
}
