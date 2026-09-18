package com.myproject.balance.controller;

import java.math.BigDecimal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.balance.dto.BalanceResponse;
import com.myproject.balance.entity.Balance;
import com.myproject.balance.service.BalanceService;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/me")
    public Balance getBalanceByUserId(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        return balanceService.getBalanceByUserId(userId);
    }

    @PostMapping("/deduct")
    public BalanceResponse deductBalance(@AuthenticationPrincipal Jwt jwt, @RequestParam BigDecimal amount) {
        String userId = jwt.getClaimAsString("userId");
        return balanceService.deductBalance(userId, amount);
    }
}