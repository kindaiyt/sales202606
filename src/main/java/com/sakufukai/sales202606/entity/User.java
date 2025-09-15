package com.sakufukai.sales202606.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users") // User は予約語なので users に変更
@Getter
@Setter
public class User {
    @Id
    @Column(nullable = false, unique = true)
    private String email; // 主キーに変更

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.PENDING; // 初回は承認待ち（PENDING, USER, ADMIN）

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user")
    private List<UserStore> userStores;
}
