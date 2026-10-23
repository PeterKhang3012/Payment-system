package com.myproject.payment.controller;

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

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserClient userClient;

    public PaymentController(PaymentService paymentService, UserClient userClient) {
        this.paymentService = paymentService;
        this.userClient = userClient;
    }

    @PostMapping("/request-otp")
    public GenerateOtpResponse requestOtp(
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {

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
            @RequestBody VerifyOtpRequest request,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("userId");

        paymentService.verifyOtpAndPay(
                userId,
                authorization,
                request.paymentId(),
                request.code()
            );
    }
}
