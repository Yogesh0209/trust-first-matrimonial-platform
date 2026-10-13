package com.tfmsp.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:admin@milan.com}") String adminEmail,
            @Value("${app.admin.password:Admin@1234}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setFullName("Platform Admin");
        admin.setEmail(adminEmail);
        admin.setPhone("9000000000");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.SUPER_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(true);
        userRepository.save(admin);
        System.out.println("Super Admin created: " + adminEmail);
    }
}