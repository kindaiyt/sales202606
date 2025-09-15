package com.sakufukai.sales202606.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_store")
@Getter
@Setter
public class UserStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 側との紐付け
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Store 側との紐付け
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;
}
