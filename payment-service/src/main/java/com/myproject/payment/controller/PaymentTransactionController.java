package com.myproject.payment.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.entity.PaymentTransaction;
import com.myproject.payment.service.PaymentTransactionService;

@RestController
@RequestMapping("/api/payment-transactions")
public class PaymentTransactionController {
    private final PaymentTransactionService paymentTransactionService;

    public PaymentTransactionController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @GetMapping("/history")
    public List<PaymentTransaction> getHistoryList(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        return paymentTransactionService.getPaymentTransactionsByUserId(userId);
    }
}
