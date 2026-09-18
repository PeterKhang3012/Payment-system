package com.myproject.balance.service;

import org.springframework.stereotype.Service;

import com.myproject.balance.dto.BalanceResponse;
import com.myproject.balance.entity.Balance;
import com.myproject.balance.repository.BalanceRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BalanceService {
    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    // Search for balance by userID
    public Balance getBalanceByUserId(String userId) {
        return balanceRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Balance not found for user ID: " + userId));
    }

    public BalanceResponse deductBalance(String userId, java.math.BigDecimal amount) {
        Balance balance = getBalanceByUserId(userId);
        if (balance.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance for user ID: " + userId);
        }
        balance.setBalance(balance.getBalance().subtract(amount));
        balanceRepository.save(balance);
        return new BalanceResponse(true, "Balance deducted successfully", balance.getBalance());
    }
}

