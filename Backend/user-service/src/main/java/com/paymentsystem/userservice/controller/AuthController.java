package com.paymentsystem.userservice.controller;

import com.paymentsystem.userservice.dto.UserDtos.LoginRequest;
import com.paymentsystem.userservice.dto.UserDtos.LoginResponse;
import com.paymentsystem.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Đăng nhập — endpoint public, không cần token. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Dang nhap va quan ly JWT token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Dang nhap",
        description = "Xac thuc username/password, tra ve JWT token.\n\n"
                    + "Tai khoan mau: nguyenvana / Student@123 | tranthib / Student@123 | lequanghung / Student@123")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thanh cong, tra ve JWT token"),
        @ApiResponse(responseCode = "400", description = "Username hoac password de trong"),
        @ApiResponse(responseCode = "401", description = "Sai username hoac password")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
