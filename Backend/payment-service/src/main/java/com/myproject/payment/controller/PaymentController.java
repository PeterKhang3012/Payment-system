package com.myproject.payment.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.dto.UserDtos;
import com.myproject.payment.client.UserClient;
import com.myproject.payment.dto.OtpDtos.GenerateOtpResponse;
import com.myproject.payment.dto.OtpDtos.VerifyOtpRequest;
import com.myproject.payment.dto.PaymentRequest;
import com.myproject.payment.service.PaymentService;
import com.myproject.payment.exception.BadRequestException;

@RestController
@RequestMapping("/api/payments")
// API validation: use MVC boundary validation without a separate method-validation proxy.
public class PaymentController {

    private final PaymentService paymentService;
    private final UserClient userClient;

    public PaymentController(PaymentService paymentService, UserClient userClient) {
        this.paymentService = paymentService;
        this.userClient = userClient;
    }

    @PostMapping("/request-otp")
    public GenerateOtpResponse requestOtp(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {

        if (paymentRequest == null) {
            throw new BadRequestException("Request body is required");
        }

        if (paymentRequest.getStudentId() == null
                || paymentRequest.getStudentId() <= 0) {
            throw new BadRequestException("Invalid studentId");
        }

        if (!paymentRequest.isAcceptedTerms()) {
            throw new BadRequestException(
                "Terms and conditions must be accepted");
        }
        String userId = jwt.getClaimAsString("userId");
        UserDtos.UserResponse user = userClient.getEmailByUserId(authorization);
        String email = user.email();

        return paymentService.requestOtp(
                userId,
                paymentRequest.getStudentId(),
                paymentRequest.isAcceptedTerms(),
                authorization,
                email);
    }

    @PostMapping("/verify")
    public void verifyOtpAndPay(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        if (request.paymentId() == null|| request.paymentId().isBlank()) {
            throw new BadRequestException("Invalid paymentId");
        }

        if (request.code()==null) {
            throw new BadRequestException("Invalid code");
        }

        String userId = jwt.getClaimAsString("userId");

        paymentService.verifyOtpAndPay(
                userId,
                authorization,
                request.paymentId(),
                request.code()
            );
    }
}
