package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.constants.AuthConstants;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final TokenService tokenService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.activation-token.expiration-ms}")
    private long activationTokenExpirationMs;


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
        user.setStatus(AuthConstants.STATUS_INACTIVE);

        try {
            User saved = userRepository.save(user);
            log.info("User registered (pending activation): {}", saved.getEmail());

            String activationToken = generateSecureToken();
            System.out.println("Activation Token:"+ activationToken);
            tokenService.storeActivationToken(activationToken, saved.getUserId(), activationTokenExpirationMs);
            String activationUrl = baseUrl + "/api/v1/user/activate?token=" + activationToken;
            emailService.sendActivationEmail(saved.getEmail(), activationUrl);

            return UserResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate email registration attempt: {}", normalizedEmail);
            throw new BadRequestException("Email already registered");
        }
    }


    @Transactional
    public void activateAccount(String token) {
        UUID userId = tokenService.validateActivationToken(token);
        if (userId == null) {
            throw new BadRequestException("Invalid or expired or already used activation link");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (AuthConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            tokenService.deleteActivationToken(token);
            return;
        }

        user.setStatus(AuthConstants.STATUS_ACTIVE);
        userRepository.save(user);
        tokenService.deleteActivationToken(token);
        log.info("Account activated for user: {}", user.getEmail());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
