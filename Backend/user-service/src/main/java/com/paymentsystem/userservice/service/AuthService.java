package com.paymentsystem.userservice.service;

import com.paymentsystem.userservice.dto.UserDtos.LoginRequest;
import com.paymentsystem.userservice.dto.UserDtos.LoginResponse;
import com.paymentsystem.userservice.dto.UserDtos.UserResponse;
import com.paymentsystem.userservice.entity.User;
import com.paymentsystem.userservice.exception.GlobalException;
import com.paymentsystem.userservice.repository.UserRepository;
import com.paymentsystem.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/** Xử lý đăng nhập và sinh JWT token. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserRepository        userRepository;

    /**
     * Xác thực username/password, trả về JWT + thông tin user.
     * BadCredentialsException được bắt bởi GlobalException.Handler.
     */
    public LoginResponse login(LoginRequest request) {
        log.info("login: dang xac thuc user '{}'", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtTokenProvider.generateToken(request.username());

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new GlobalException.ResourceNotFoundException("User", "username", request.username()));

        log.info("login: thanh cong user='{}'", request.username());

        return new LoginResponse(token, "Bearer", jwtTokenProvider.getExpirationMs() / 1000, toUserResponse(user));
    }

    /** Convert User entity → UserResponse DTO (không chứa passwordHash). */
    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
