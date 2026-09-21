package com.myproject.payment.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.dto.PaymentTransactionResponse;
import com.myproject.payment.service.PaymentTransactionService;

@RestController
@RequestMapping("/api/payment-transactions")
public class PaymentTransactionController {
    private final PaymentTransactionService paymentTransactionService;

    public PaymentTransactionController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @GetMapping("/history")
    public List<PaymentTransactionResponse> getHistoryList(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        return paymentTransactionService.getPaymentTransactionsByUserId(userId).stream()
                .map(transaction -> new PaymentTransactionResponse(
                        transaction.getId(),
                        transaction.getTuitionId(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
