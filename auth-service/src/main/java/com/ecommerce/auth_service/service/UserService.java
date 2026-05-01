package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.request.RegisterRequest;
import com.ecommerce.auth_service.dto.response.AddressResponse;
import com.ecommerce.auth_service.dto.response.CustomerProfileResponse;
import com.ecommerce.auth_service.dto.response.UserResponse;
import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.enums.RoleName;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.exception.ResourceNotFoundException;
import com.ecommerce.auth_service.repository.AddressRepository;
import com.ecommerce.auth_service.repository.RoleRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;


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
        user.setRole(resolveRole(request.getRole()));

        try {
            User saved = userRepository.save(user);
            log.info("User registered: {}", saved.getEmail());
            return UserResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate email registration attempt: {}", normalizedEmail);
            throw new BadRequestException("Email already registered");
        }
    }

    private Role resolveRole(RoleName requestedRole) {
        RoleName roleName = (requestedRole != null) ? requestedRole : RoleName.CUSTOMER;

        if (roleName == RoleName.ADMIN) {
            throw new BadRequestException("Admin role cannot be assigned during registration");
        }

        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + roleName));
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<AddressResponse> addresses = addressRepository.findAllByUserIdOrdered(userId)
                .stream()
                .map(AddressResponse::from)
                .toList();

        log.debug("Profile fetched for user: {}", userId);
        return CustomerProfileResponse.from(user, addresses);
    }
}
