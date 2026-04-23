package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.response.UserResponse;
import com.ecommerce.auth_service.entity.User;
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
}
