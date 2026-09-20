package com.paymentsystem.userservice.controller;

import com.paymentsystem.userservice.dto.UserDtos.UserResponse;
import com.paymentsystem.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tra cứu thông tin sinh viên — yêu cầu JWT hợp lệ. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Tra cuu thong tin sinh vien")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Lay thong tin sinh vien dang dang nhap",
               description = "Tra ve thong tin tai khoan tu JWT token.",
               security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thanh cong"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le"),
        @ApiResponse(responseCode = "404", description = "Khong tim thay user")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lay thong tin sinh vien theo ID",
               description = "Dung boi payment-service de lay email sinh vien truoc khi gui OTP.",
               security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thanh cong"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le"),
        @ApiResponse(responseCode = "404", description = "Khong tim thay user")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
