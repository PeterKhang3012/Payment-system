package com.myproject.balance.controller;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import com.myproject.balance.dto.BalanceResponse;
import com.myproject.balance.dto.DeductBalanceResponse;
import com.myproject.balance.service.BalanceService;

@RestController
@RequestMapping("/api/balances")
// API validation: native MVC method validation rejects invalid amounts and headers before service calls.
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/me")
    public BalanceResponse getBalanceByUserId(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        return balanceService.getBalanceByUserId(userId);
    }

    @PostMapping("/deduct")
    public DeductBalanceResponse deductBalance(@AuthenticationPrincipal Jwt jwt, 
                                                @RequestHeader("Idempotency-Key")  
                                                // API validation: respect the persisted idempotency key length.
                                                @jakarta.validation.constraints.Size(max = 36)
                                                @NotBlank(message = "Idempotency-Key is required")
                                                String paymentId, 

                                                @RequestBody 
                                                @NotNull(message = "Amount is required")
                                                // API validation: fit the deduction DECIMAL(19,2) before database access.
                                                @jakarta.validation.constraints.Digits(integer = 17, fraction = 2)
                                                @Positive(message = "Amount must be greater than zero")
                                                BigDecimal amount) {
        String userId = jwt.getClaimAsString("userId");
        return balanceService.deductBalance(userId, paymentId, amount);
    }
}
