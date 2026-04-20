package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.constants.AuthConstants;
import com.ecommerce.auth_service.dto.request.LoginRequest;
import com.ecommerce.auth_service.dto.response.LoginResponse;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.AccountLockedException;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

   /* public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!AuthConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            throw new BadRequestException("Account is not active. Current status: " + user.getStatus());
        }

        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Please reset your password to unlock.");
        }


    }*/

}
