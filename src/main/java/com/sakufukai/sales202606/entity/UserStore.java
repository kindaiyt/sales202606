package com.sakufukai.sales202606.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "UserStore")
public class UserStore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    // getter/setter
}
