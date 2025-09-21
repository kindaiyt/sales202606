package com.sakufukai.sales202606.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sakufukai.sales202606.entity.User;
import com.sakufukai.sales202606.entity.Role;
import com.sakufukai.sales202606.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public AdminInitializer(UserRepository userRepository, AppProperties appProperties) {
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        for (String email : appProperties.getAdminEmails()) {
            userRepository.findByEmail(email).ifPresentOrElse(
                    user -> {
                        if (user.getRole() != Role.ADMIN) {
                            user.setRole(Role.ADMIN);
                            userRepository.save(user);
                            System.out.println("Updated role to ADMIN for: " + email);
                        }
                    },
                    () -> {
                        User newAdmin = new User();
                        newAdmin.setEmail(email);
                        newAdmin.setName("（未ログイン）");
                        newAdmin.setRole(Role.ADMIN);
                        newAdmin.setCreatedAt(LocalDateTime.now());
                        newAdmin.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(newAdmin);
                        System.out.println("Created new ADMIN user: " + email);
                    }
            );
        }
    }
}
