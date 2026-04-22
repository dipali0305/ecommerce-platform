package com.ecommerce.auth_service.config;


import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.enums.RoleName;
import com.ecommerce.auth_service.repository.RoleRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    @Transactional
    protected void initRoles() {
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                log.info("Created role: {}", roleName);
            }
        });
    }

    protected void initAdminUser() {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin user already exists, skipping creation");
            return;
        }
        if (adminPassword == null || adminPassword.length() < 8) {
            throw new IllegalStateException(
                    "Admin password must be at least 8 characters. Set ADMIN_PASSWORD environment variable.");
        }

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Role ADMIN not found. Ensure initRoles() ran successfully."));

        Set<Role> adminRoles = Set.of(adminRole);
        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(adminEmail.toLowerCase().trim());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRoles(adminRoles);

        userRepository.save(admin);
        log.info("Default admin user created with email: {} and roles: {}", adminEmail, adminRoles.size());
    }
}
