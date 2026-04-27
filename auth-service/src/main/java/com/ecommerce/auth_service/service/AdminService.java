package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.response.UserResponse;
import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.enums.RoleName;
import com.ecommerce.auth_service.exception.ResourceNotFoundException;
import com.ecommerce.auth_service.repository.RoleRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.auth_service.exception.BadRequestException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        return userRepository.findAll(pageRequest).map(UserResponse::adminView);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = findUserOrThrow(userId);
        return UserResponse.adminView(user);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Transactional
    public void unlockUser(UUID userId) {
        User user = findUserOrThrow(userId);

        if (!user.isAccountLocked()) {
            log.info("Account already unlocked for user: {}", userId);
            return;
        }

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedAt(null);
        userRepository.save(user);
        log.info("Account unlocked by admin for user: {}", userId);
    }

    public UserResponse updateUserRole(UUID adminUserId,UUID userId, RoleName roleName) {
        UserResponse response = updateRoleTransactional(adminUserId,userId, roleName);
        tokenService.revokeAllTokens(userId);
        return response;
    }

    @Transactional
    protected UserResponse updateRoleTransactional(UUID adminUserId, UUID userId, RoleName roleName) {
        if (adminUserId.equals(userId)) {
            throw new BadRequestException("Admins cannot change their own role");
        }
        if (roleName == RoleName.ADMIN) {
            throw new BadRequestException("Admin role cannot be assigned");
        }
        User user = findUserOrThrow(userId);
        if (user.getRole().getRoleName() == roleName) {
            throw new BadRequestException("User already has role: " + roleName);
        }
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setRole(role);
        User saved = userRepository.save(user);
        log.info("Admin {} updated role of user {} to {}", adminUserId, userId, roleName);
        return UserResponse.adminView(saved);
    }

    private Role findRoleOrThrow(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }
}
