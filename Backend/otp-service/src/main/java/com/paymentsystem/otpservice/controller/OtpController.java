package com.paymentsystem.otpservice.controller;

import com.paymentsystem.otpservice.dto.OtpDtos.GenerateOtpRequest;
import com.paymentsystem.otpservice.dto.OtpDtos.GenerateOtpResponse;
import com.paymentsystem.otpservice.dto.OtpDtos.VerifyOtpRequest;
import com.paymentsystem.otpservice.dto.OtpDtos.VerifyOtpResponse;
import com.paymentsystem.otpservice.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 2 endpoint nội bộ cho payment-service. Yêu cầu header X-Internal-Service-Token. */
@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@Tag(name = "OTP", description = "API noi bo — sinh va xac thuc OTP (chi danh cho payment-service)")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/generate")
    @Operation(
        summary = "Sinh va gui OTP",
        description = "Sinh OTP 6 chu so (SecureRandom), luu DB expiry 5 phut, gui email.\n"
                    + "Neu paymentId da ton tai OTP cu → OTP moi ghi de.\n"
                    + "Header: X-Internal-Service-Token: ps-internal-service-token-2025")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Thanh cong"),
        @ApiResponse(responseCode = "400", description = "paymentId trong hoac email sai dinh dang"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le"),
        @ApiResponse(responseCode = "503", description = "SMTP loi")
    })
    public ResponseEntity<GenerateOtpResponse> generate(@RequestBody @Valid GenerateOtpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(otpService.generateOtp(request));
    }

    @PostMapping("/verify")
    @Operation(
        summary = "Xac thuc OTP",
        description = "Atomic UPDATE de verify — ngan race condition va brute-force.\n"
                    + "success=true: dung. success=false + remainingAttempts: sai nhung chua khoa.\n"
                    + "Header: X-Internal-Service-Token: ps-internal-service-token-2025")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verify thuc hien (xem truong success)"),
        @ApiResponse(responseCode = "400", description = "code khong phai 6 chu so"),
        @ApiResponse(responseCode = "401", description = "Token khong hop le"),
        @ApiResponse(responseCode = "404", description = "Khong tim thay OTP"),
        @ApiResponse(responseCode = "409", description = "OTP da duoc su dung"),
        @ApiResponse(responseCode = "410", description = "OTP da het han"),
        @ApiResponse(responseCode = "429", description = "OTP bi khoa (sai >= 5 lan)")
    })
    public ResponseEntity<VerifyOtpResponse> verify(@RequestBody @Valid VerifyOtpRequest request) {
        return ResponseEntity.ok(otpService.verifyOtp(request));
    }
}
