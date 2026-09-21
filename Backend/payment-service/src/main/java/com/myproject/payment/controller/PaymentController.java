package com.myproject.payment.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.dto.PaymentRequest;
import com.myproject.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public void payTuition(@RequestBody PaymentRequest paymentRequest,  @RequestHeader("Authorization") String authorization, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        paymentService.payTuition(userId, paymentRequest.getStudentId(), paymentRequest.isAcceptedTerms(), authorization);
    }
}
