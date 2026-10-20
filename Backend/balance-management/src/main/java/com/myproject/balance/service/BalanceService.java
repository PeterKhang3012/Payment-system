package com.myproject.balance.service;

import org.springframework.stereotype.Service;

import com.myproject.balance.dto.BalanceResponse;
import com.myproject.balance.dto.DeductBalanceResponse;
import com.myproject.balance.entity.Balance;
import com.myproject.balance.entity.BalanceDeduction;
import com.myproject.balance.exception.BalanceNotFoundException;
import com.myproject.balance.exception.InsufficientBalanceException;
import com.myproject.balance.exception.PaymentDeductionConflictException;
import com.myproject.balance.repository.BalanceRepository;
import com.myproject.balance.repository.BalanceDeductionRepository;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class BalanceService {
    private final BalanceRepository balanceRepository;
    private final BalanceDeductionRepository balanceDeductionRepository;

    public BalanceService(BalanceRepository balanceRepository,
                        BalanceDeductionRepository balanceDeductionRepository) {
        this.balanceRepository = balanceRepository;
        this.balanceDeductionRepository = balanceDeductionRepository;
    }

    // Search for balance by userID
    public BalanceResponse getBalanceByUserId(String userId) {
        Balance balance =balanceRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BalanceNotFoundException("Balance not found for user ID: " + userId));
        return new BalanceResponse(balance.getBalance());
    }

    public DeductBalanceResponse deductBalance(String userId, String paymentId, java.math.BigDecimal amount) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment ID must not be blank.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                "Amount must be greater than zero"
            );
        }

        Balance balance = balanceRepository.findByUserIdForUpdate(userId)
        .orElseThrow(() ->
            new BalanceNotFoundException("Balance not found for user ID: " + userId)
        );

        Optional<BalanceDeduction> existing = balanceDeductionRepository.findByPaymentId(paymentId);

        if (existing.isPresent()) {
            BalanceDeduction previous = existing.get();

            // Không cho phép dùng lại paymentId cho giao dịch khác.
            if (!previous.getUserId().equals(userId)
                    || previous.getAmount().compareTo(amount) != 0) {
                throw new PaymentDeductionConflictException(
                        "Payment ID was already used for a different deduction.");
            }

            // Giao dịch đã thành công: trả kết quả cũ, không trừ tiền lần nữa.
            return new DeductBalanceResponse(
                    true,
                    "Payment was already processed.",
                    previous.getBalanceAfterDeduction());
        }

        if (balance.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                "Insufficient balance to deduct " + amount
            );
        }
        BigDecimal newBalance = balance.getBalance().subtract(amount);
        balance.setBalance(newBalance);
        balanceRepository.save(balance);

        balanceDeductionRepository.saveAndFlush(new BalanceDeduction(
            paymentId, 
            userId,
            amount, 
            newBalance)
        );
        return new DeductBalanceResponse(true, "Balance deducted successfully", balance.getBalance());
    }
}

