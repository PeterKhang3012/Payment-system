package com.myproject.balance.service;

import org.springframework.stereotype.Service;

import com.myproject.balance.dto.BalanceResponse;
    import com.myproject.balance.dto.DeductBalanceResponse;
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
    public BalanceResponse getBalanceByUserId(String userId) {
        Balance balance =balanceRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Balance not found for user ID: " + userId));
        return new BalanceResponse(balance.getBalance());
    }

    public DeductBalanceResponse deductBalance(String userId, java.math.BigDecimal amount) {
        Balance balance = balanceRepository.findByUserIdForUpdate(userId)
        .orElseThrow(() ->
            new RuntimeException("Balance not found for user ID: " + userId)
        );
        balance.setBalance(balance.getBalance().subtract(amount));
        balanceRepository.save(balance);
        return new DeductBalanceResponse(true, "Balance deducted successfully", balance.getBalance());
    }
}

