package com.wallet.service;

import com.wallet.model.User;
import com.wallet.repo.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private final UserRepo users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminAccountInitializer(UserRepo users, PasswordEncoder passwordEncoder,
                                   @Value("${wallet.admin.email:}") String email,
                                   @Value("${wallet.admin.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Set both WALLET_ADMIN_EMAIL and a WALLET_ADMIN_PASSWORD of at least 12 characters");
        }
        users.findByEmail(email).ifPresentOrElse(user -> {
            user.setRole("ADMIN");
            users.save(user);
        }, () -> {
            User admin = new User();
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole("ADMIN");
            users.save(admin);
        });
    }
}
