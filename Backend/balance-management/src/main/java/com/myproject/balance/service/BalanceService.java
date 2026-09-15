package com.myproject.balance.service;

import org.springframework.stereotype.Service;

import com.myproject.balance.entity.Balance;
import com.myproject.balance.repository.BalanceRepository;

import jakarta.transaction.Transactional;

@Service
public class BalanceService {
    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    // Search for balance by userID
    @Transactional
    public Balance getBalanceByUserId(String userId) {
        return balanceRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Balance not found for user ID: " + userId));
    }
}

