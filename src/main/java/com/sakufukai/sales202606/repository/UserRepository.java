package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    List<User> findByRoleNot(Role role);
    List<User> findAllByOrderBySortOrderAscEmailAsc();

}
