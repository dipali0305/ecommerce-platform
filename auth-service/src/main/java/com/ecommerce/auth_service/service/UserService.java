package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.request.RegisterRequest;
import com.ecommerce.auth_service.dto.response.UserResponse;
import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.enums.RoleName;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.exception.ResourceNotFoundException;
import com.ecommerce.auth_service.repository.RoleRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(resolveRoles(request.getRole()));

        try {
            User saved = userRepository.save(user);
            log.info("User registered: {}", saved.getEmail());
            return UserResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate email registration attempt caught by DB constraint: {}", normalizedEmail);
            throw new BadRequestException("Email already registered");
        }
    }

    private Set<Role> resolveRoles(RoleName requestedRole) {
        RoleName roleName = (requestedRole != null) ? requestedRole : RoleName.CUSTOMER;
        if (roleName == RoleName.ADMIN) {
            throw new BadRequestException("Admin role cannot be assigned during registration");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "System role " + roleName + " not configured"));

        return Set.of(role);
    }
}
