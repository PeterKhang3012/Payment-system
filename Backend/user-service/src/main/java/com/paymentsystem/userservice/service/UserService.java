package com.paymentsystem.userservice.service;

import com.paymentsystem.userservice.dto.UserDtos.UserResponse;
import com.paymentsystem.userservice.entity.User;
import com.paymentsystem.userservice.exception.GlobalException;
import com.paymentsystem.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tra cứu thông tin sinh viên. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Lấy thông tin sinh viên đang đăng nhập từ JWT principal. */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new GlobalException.ResourceNotFoundException(
                        "User", "username", userDetails.getUsername()));
        return AuthService.toUserResponse(user);
    }

    /** Lấy thông tin sinh viên theo ID — dùng bởi payment-service. */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalException.ResourceNotFoundException("User", "id", id));
        return AuthService.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByStudentId(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new GlobalException.ResourceNotFoundException("User", "studentId", studentId));
        return AuthService.toUserResponse(user);
    }
}
