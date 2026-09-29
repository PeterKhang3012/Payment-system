package com.myproject.payment.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.dto.OtpDtos.GenerateOtpResponse;
import com.myproject.payment.dto.OtpDtos.VerifyOtpRequest;
import com.myproject.payment.dto.PaymentRequest;
import com.myproject.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/request-otp")
    public GenerateOtpResponse requestOtp(
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String authorization,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("userId");
        String email = "9conso@gmail.com";

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
